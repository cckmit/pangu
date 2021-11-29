package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.UnitRate;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackBeforePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.ZhiSiYiJiParam;
import com.pangu.framework.utils.math.RandomUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 致死一击
 * 有10%概率对敌方单位造成毁灭性的致命一击
 * 2级:触发后该次攻击必定暴击，暴击伤害额外提高50%
 * 2级:触发后该次攻击必定暴击，暴击伤害额外提高100%
 * 2级:触发后该次攻击必定暴击，暴击伤害额外提高200%
 */
@Component
public class ZhiSiYiJi implements AttackBeforePassive {
    @Override
    public PassiveType getType() {
        return PassiveType.ZHI_SI_YI_JI;
    }

    @Override
    public void attackBefore(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time, Context context, SkillReport skillReport) {
        if (effectState.getType() != EffectType.HP_M_DAMAGE && effectState.getType() != EffectType.HP_P_DAMAGE) {
            return;
        }
        ZhiSiYiJiParam param = passiveState.getParam(ZhiSiYiJiParam.class);
        double rate = param.getRate();
        Boolean valid = passiveState.getAddition(boolean.class);
        if (!RandomUtils.isHit(rate)) {
            if (valid != null && valid) {
                passiveState.setAddition(false);

                Map<UnitRate, Double> addRate = param.getAddRate();
                for (Map.Entry<UnitRate, Double> e : addRate.entrySet()) {
                    UnitRate unitRate = e.getKey();
                    Double rateValue = e.getValue();
                    owner.increaseRate(unitRate, -rateValue);
                }
            }
            return;
        }
        if (valid == null || !valid) {
            passiveState.setAddition(true);
            Map<UnitRate, Double> addRate = param.getAddRate();
            for (Map.Entry<UnitRate, Double> e : addRate.entrySet()) {
                UnitRate unitRate = e.getKey();
                Double rateValue = e.getValue();
                owner.increaseRate(unitRate, rateValue);
            }
        }
    }

    @Override
    public void attackEnd(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time, Context context, SkillReport skillReport) {
        Boolean valid = passiveState.getAddition(boolean.class);
        if (valid == null || !valid) {
            return;
        }
        passiveState.setAddition(false);
        ZhiSiYiJiParam param = passiveState.getParam(ZhiSiYiJiParam.class);
        Map<UnitRate, Double> addRate = param.getAddRate();
        for (Map.Entry<UnitRate, Double> e : addRate.entrySet()) {
            UnitRate unitRate = e.getKey();
            Double rateValue = e.getValue();
            owner.increaseRate(unitRate, -rateValue);
        }
        passiveState.addCD(time);
    }
}
