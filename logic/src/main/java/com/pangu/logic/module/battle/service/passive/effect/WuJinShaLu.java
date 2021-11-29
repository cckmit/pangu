package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.UnitRate;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackBeforePassive;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.WuJinShaLuParam;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 无尽杀戮的深渊: <br>
 * 对目标造成140%攻击力无视防御与免疫的伤害,如果目标生命值低于40%则造成4倍伤害 <br>
 * 2级:成功击杀目标时,额外恢复250点能量如果被击杀的目标是召唤物则变为额外恢复700点能量 <br>
 * 3级:成功击杀目标时,额外获得一个350%攻击力的护盾 <br>
 * 4级:伤害提升至170% <br>
 */
@Component("PASSIVE:WuJinShaLu")
public class WuJinShaLu implements AttackBeforePassive, AttackPassive {
    //进攻前将目标的防御和减伤降为0，并添加无敌失效状态
    @Override
    public void attackBefore(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time, Context context, SkillReport skillReport) {
        final WuJinShaLuParam param = passiveState.getParam(WuJinShaLuParam.class);
        final SkillState skillState = context.getRootSkillEffectAction().getSkillState();
        if (!skillState.getTag().equals(param.getTriggerSkillTag())) {
            return;
        }

        final Addition addition = passiveState.getAddition(Addition.class, new Addition());
        addition.clear();
        //修改目标防御至0
        final long defenceM = target.getValue(UnitValue.DEFENCE_M);
        final long defenceP = target.getValue(UnitValue.DEFENCE_P);
        final double unHarmM = target.getRate(UnitRate.UNHARM_M);
        final double unHarmP = target.getRate(UnitRate.UNHARM_P);
        target.setValue(UnitValue.DEFENCE_M, 0);
        target.setValue(UnitValue.DEFENCE_P, 0);
        target.setRate(UnitRate.UNHARM_M, 0);
        target.setRate(UnitRate.UNHARM_P, 0);
        addition.valueAlters.put(UnitValue.DEFENCE_M, defenceM);
        addition.valueAlters.put(UnitValue.DEFENCE_P, defenceP);
        addition.rateAlters.put(UnitRate.UNHARM_M, unHarmM);
        addition.rateAlters.put(UnitRate.UNHARM_P, unHarmP);

        //为目标添加无敌无效状态
        target.addState(UnitState.WU_DI_INVALID, time);
        addition.stateAlters.put(UnitState.WU_DI_INVALID, time);
    }

    //进攻后还原
    @Override
    public void attackEnd(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time, Context context, SkillReport skillReport) {
        final WuJinShaLuParam param = passiveState.getParam(WuJinShaLuParam.class);
        final SkillState skillState = context.getRootSkillEffectAction().getSkillState();
        if (!skillState.getTag().equals(param.getTriggerSkillTag())) {
            return;
        }

        final Addition addition = passiveState.getAddition(Addition.class, new Addition());

        //还原目标防御力
        for (Map.Entry<UnitRate, Double> entry : addition.rateAlters.entrySet()) {
            target.setRate(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<UnitValue, Long> entry : addition.valueAlters.entrySet()) {
            target.setValue(entry.getKey(), entry.getValue());
        }

        //移除目标无敌失效状态
        for (Map.Entry<UnitState, Integer> entry : addition.stateAlters.entrySet()) {
            target.removeState(entry.getKey(), entry.getValue());
        }
    }

    private static class Addition {
        private Map<UnitValue, Long> valueAlters = new HashMap<>();
        private Map<UnitRate, Double> rateAlters = new HashMap<>();
        private Map<UnitState, Integer> stateAlters = new HashMap<>();

        void clear() {
            valueAlters.clear();
            rateAlters.clear();
            stateAlters.clear();
        }
    }

    //对残血目标增伤
    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (damage >= 0) {
            return;
        }

        final WuJinShaLuParam param = passiveState.getParam(WuJinShaLuParam.class);

        if (!skillState.getTag().equals(param.getTriggerSkillTag())) {
            return;
        }

        if (target.getHpPct() >= param.getSlayHpPct()) {
            return;
        }

        final long dmgChange = (long) (damage * (param.getSlayDmgRate() - 1));
        PassiveUtils.hpUpdate(context, skillReport, target, dmgChange, time);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.WU_JIN_SHA_LU;
    }
}
