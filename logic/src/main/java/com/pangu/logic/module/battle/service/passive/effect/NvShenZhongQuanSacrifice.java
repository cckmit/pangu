package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.values.Death;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.OwnerDiePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.NvShenZhongQuanSacrificeParam;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 女神忠犬
 * 战斗开始时,消耗自身60%当前生命值,召唤出3个小狗,小狗持续自动袭击敌人造成50%攻击力的伤害；小狗回到身边会回复造成伤害35%的生命值；当受到致死伤害时,会立即献祭一个小狗自身回复25%最大生命值
 * 2级:伤害提升至60%攻击力
 * 3级:伤害提升至70%攻击力
 * 4级:伤害提升至80%攻击力
 */
@Component
public class NvShenZhongQuanSacrifice implements OwnerDiePassive {
    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attack, ITimedDamageReport timedDamageReport, int time, Context context) {
        //不存在召唤单位不做任何处理
        final List<Unit> livingSummonedUnits = owner.getFriend().getCurrent().stream().filter(unit -> unit.getSummonUnit() == owner && !unit.isDead()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(livingSummonedUnits)) {
            return;
        }
        final boolean success = owner.revive(time);
        final String psvId = passiveState.getId();
        if (!success) {
            for (Unit sacrificed : livingSummonedUnits) {
                sacrificed.dead();
                timedDamageReport.add(time, sacrificed.getId(), PassiveValue.single(psvId, owner.getId(), new Death()));
            }
            return;
        }

        //献祭召唤单位
        final Unit sacrificed = livingSummonedUnits.get(0);
        sacrificed.dead();
        timedDamageReport.add(time, sacrificed.getId(), PassiveValue.single(psvId, owner.getId(), new Death()));
        //回复生命
        final NvShenZhongQuanSacrificeParam param = passiveState.getParam(NvShenZhongQuanSacrificeParam.class);
        final Double maxHpPct = param.getRecoverRate();
        final long value = owner.getValue(UnitValue.HP) + context.getHpChange(owner);
        //添加修改其他属性
        context.modVal(owner, owner, time, timedDamageReport, param.getValModParam(), psvId, null);

        // 保留1点血
        context.addPassiveValue(owner, AlterType.HP, -value + 1);
//        timedDamageReport.add(time, owner.getId(), Hp.of(-value + 1));
        final long hpChange = (long) (maxHpPct * owner.getValue(UnitValue.HP_MAX));
        context.passiveRecover(owner, owner, hpChange, time, passiveState, timedDamageReport);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.NV_SHEN_ZHONG_QUAN_SACRIFICE;
    }
}
