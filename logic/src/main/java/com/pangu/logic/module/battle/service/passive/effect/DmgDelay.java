package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Immune;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.UnitHpChangePassive;
import com.pangu.logic.module.battle.service.passive.param.DmgDelayParam;
import com.pangu.framework.utils.math.RandomUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;

/**
 * 受到攻击时有一定概率将后5秒内受到的伤害延迟到第5秒一次性结算
 */
@Component
public class DmgDelay implements UnitHpChangePassive, DamagePassive {
    @Override
    public PassiveType getType() {
        return PassiveType.DMG_DELAY;
    }

    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final DmgDelayParam param = passiveState.getParam(DmgDelayParam.class);
        final Addition addition = passiveState.getAddition(Addition.class, new Addition());

        if (owner.getHpPct() > param.getTriggerWhenHpPctLessThan()) {
            return;
        }
        if (time <= addition.cdEndTime) {
            return;
        }
        if (!RandomUtils.isHit(param.getProb())) {
            return;
        }

        //生成一个buff作为定时炸弹，用于缓存并引爆伤害
        BuffFactory.addBuff(param.getBuff(), owner, owner, time, skillReport, null);

        //设置cd
        addition.cdEndTime = time + param.getCd();
    }

    @Override
    public void hpChange(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> changeUnit) {
        if (owner.isDead()) {
            return;
        }

        //自身若未受伤，无需进行任何操作
        final long actualHpChange = context.getActualHpChange(owner);
        //  本次结算周期间所受到的伤害
        final long curDmg = Math.min(0, context.getHpChange(owner));
        //  对冲数值
        final long offset = -curDmg - actualHpChange;
        if (offset <= 0) {
            return;
        }

        //将伤害对冲并缓存至特定buff中
        final DmgDelayParam param = passiveState.getParam(DmgDelayParam.class);
        final List<BuffState> buffStates = owner.getBuffBySettingId(param.getBuff());
        if (CollectionUtils.isEmpty(buffStates)) {
            return;
        }
        final BuffState bomb = buffStates.get(0);
        Long accDmg = bomb.getAddition(Long.class, 0L);
        accDmg -= offset;
        bomb.setAddition(accDmg);

        context.addPassiveValue(owner, AlterType.HP, offset);
        final String ownerId = owner.getId();
        final PassiveValue pv = PassiveValue.of(passiveState.getId(), ownerId);
//        pv.add(Hp.of(offset));
        pv.add(new Immune());
        damageReport.add(time, ownerId, pv);
    }

    private static class Addition {
        private int cdEndTime = -1;
    }
}
