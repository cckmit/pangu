package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.UnitRate;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.DispelType;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackBeforePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.RuoDianSheJiParam;
import org.springframework.stereotype.Component;


/**
 * 弱点射击:
 * 每进行7次普通攻击,下一次普攻必定暴击
 * 2级:次数缩短至5次
 * 3级:如果目标身上有负面状态时,1次普攻当2次计算
 * 4级:次数缩短至4次
 */
@Component
public class RuoDianSheJi implements AttackBeforePassive {
    @Override
    public void attackBefore(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time, Context context, SkillReport skillReport) {
        if (!effectState.getId().startsWith("JIUCHENGCIMI_NORMAL")) {
            return;
        }
        final RuoDianSheJiParam param = passiveState.getParam(RuoDianSheJiParam.class);
        final Addition addition = getAddtion(passiveState);

        //未满足触发条件
        if (addition.count <= param.getTriggerCount()) {
            return;
        }
        //满足触发条件
        addition.count = 0;
        addition.triggered = true;
        owner.increaseRate(UnitRate.CRIT, param.getCritProbUp());
    }

    @Override
    public void attackEnd(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time, Context context, SkillReport skillReport) {
        if (!effectState.getId().startsWith("JIUCHENGCIMI_NORMAL")) {
            return;
        }
        final Addition addition = getAddtion(passiveState);
        final RuoDianSheJiParam param = passiveState.getParam(RuoDianSheJiParam.class);
        //触发一次后重置暴击率
        if (addition.triggered) {
            owner.increaseRate(UnitRate.CRIT, -param.getCritProbUp());
            addition.triggered = false;
        }
        //目标身上存在负面状态则算作触发两次
        final boolean deBuffBonus = param.isDeBuffBonus();
        if (deBuffBonus && (target.getStates(true, time).size() > 0 || (target.getBuffByDispel(DispelType.HARMFUL) != null && (target.getBuffByDispel(DispelType.HARMFUL).size() > 0)))) {
            addition.count += param.getCountAddWhenDeBuff();
        } else {
            addition.count++;
        }
    }

    @Override
    public PassiveType getType() {
        return PassiveType.RUO_DIAN_SHE_JI;
    }

    private Addition getAddtion(PassiveState passiveState) {
        Addition addition = passiveState.getAddition(Addition.class);
        if (addition == null) {
            addition = new Addition();
            passiveState.setAddition(addition);
        }
        return addition;
    }

    private static class Addition {
        private int count;
        private boolean triggered;
    }
}
