package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.model.report.values.Mp;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.alter.MpAlter;
import com.pangu.logic.module.battle.service.buff.utils.CalTypeHelper;
import com.pangu.logic.module.battle.service.buff.utils.CalValues;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.UnitHpChangePassive;
import com.pangu.logic.module.battle.service.passive.param.KuangBaoParam;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * 血量每下降一定值，面板提升一定幅度
 */
@Component
public class KuangBao implements UnitHpChangePassive {
    @Override
    public PassiveType getType() {
        return PassiveType.KUANG_BAO;
    }

    @Override
    public void hpChange(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> changeUnit) {
        //此被动只关心持有者的生命状态
        if (!changeUnit.contains(owner)) {
            return;
        }

        final Addition addition = passiveState.getAddition(Addition.class, new Addition());
        //获取上轮hp
        Long pre = addition.preHp;
        final long hpMax = owner.getValue(UnitValue.HP_MAX);
        if (pre == null) {
            pre = hpMax;
        }
        //记录当前hp
        final long cur = owner.getValue(UnitValue.HP);
        addition.preHp = cur;

        //获取本轮hp变化
        final long change = cur - pre;

        //只关心hp减少
        if (change >= 0) {
            return;
        }

        final KuangBaoParam param = passiveState.getParam(KuangBaoParam.class);
        final long triggerHpCut = param.getTriggerHpCut();
        final double triggerHpPctCut = param.getTriggerHpPctCut();
        long triggerTimes = 0;
        if (triggerHpPctCut > 0) {
            //计算奖励实施次数
            final long totalChange = change + addition.remainder;
            final double totalPctChange = totalChange / 1.0 / hpMax;
            final double dTriggerTimes = totalPctChange / -triggerHpPctCut;
            triggerTimes = (long) dTriggerTimes;
            //缓存实施后的零头
            addition.remainder = (long) ((dTriggerTimes - triggerTimes) * triggerHpPctCut * hpMax);
        } else if (triggerHpCut > 0) {
            //计算奖励实施次数
            final long totalChange = change + addition.remainder;
            triggerTimes = totalChange / -triggerHpCut;
            //缓存实施后的零头
            addition.remainder = totalChange % triggerHpCut;
        }

        if (triggerTimes <= 0) {
            return;
        }

        //实施奖励
        addition.bonusCount += triggerTimes;
        final CalValues calValues = CalTypeHelper.calValues(param.getCalType(), param.getAlters(), owner, owner, param.getFactor());
        final Map<AlterType, Number> values = calValues.getValues();
        final Number mp = values.remove(AlterType.MP);
        final Number hp = values.remove(AlterType.HP);
        PassiveValue ps = PassiveValue.of(passiveState.getId(), owner.getId());

        if (mp != null) {
            final long mpForReport = MpAlter.calMpChange(owner, mp.longValue());
            context.addValue(owner, AlterType.MP, mp.longValue() * triggerTimes);
            ps.add(new Mp(mpForReport * triggerTimes));
        }
        if (hp != null) {
            context.addValue(owner, AlterType.MP, hp.longValue() * triggerTimes);
            ps.add(Hp.of(hp.longValue() * triggerTimes));
        }
        for (Map.Entry<AlterType, Number> entry : values.entrySet()) {
            final AlterType alterType = entry.getKey();
            final Number value = entry.getValue();
            final double totalValue = value.doubleValue() * triggerTimes;
            context.addValue(owner, alterType, totalValue);
            ps.add(new UnitValues(alterType, totalValue));
        }

        damageReport.add(time, owner.getId(), ps);
    }

    public long getBonusCount(PassiveState state) {
        final Addition addition = state.getAddition(Addition.class);
        return addition == null ? 0 : addition.bonusCount;
    }

    private static class Addition {
        /**
         * 前次血量
         */
        private Long preHp;
        /**
         * 上轮奖励执行后的零头
         */
        private long remainder;
        /**
         * 属性奖励累计生效次数
         */
        private long bonusCount;
    }
}
