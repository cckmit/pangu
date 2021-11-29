package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.UnitRate;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackBeforePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.ZhiMingJuShaParam;
import org.springframework.stereotype.Component;

/**
 * 致命狙杀:
 * 对准最远的目标,进行精度瞄准狙击,射出子弹,命中目标后爆炸,对目标造成360%物理攻击的伤害
 * 2级:距离目标越远,暴击率提升越高,最高50%
 * 3级:该技能初始附加10%暴击
 * 4级:暴击伤害提升50%"
 */
@Component
public class ZhiMingJuSha implements AttackBeforePassive {
    @Override
    public void attackBefore(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time, Context context, SkillReport skillReport) {
        if (!effectState.getId().startsWith("ZHI_MING_JU_SHA")) {
            return;
        }
        final ZhiMingJuShaParam param = passiveState.getParam(ZhiMingJuShaParam.class);
        final int distance = owner.getPoint().distance(target.getPoint());
        double distanceCritUp = 0;
        if (param.getStep() != 0) {
            distanceCritUp = Math.min(((double) distance / param.getStep()) * param.getCritRateUpPerStep(), param.getMaxCritRateUp());
        }
        final double totalCritUp = distanceCritUp + param.getBaseCritRateUp();
        owner.increaseRate(UnitRate.CRIT, totalCritUp);
        owner.increaseRate(UnitRate.CRIT_DAMAGE, param.getCritDmgRateUp());

        final Addition addition = getAddition(passiveState);
        addition.critProbUp = totalCritUp;
        addition.critDmgRateUp = param.getCritDmgRateUp();
    }

    @Override
    public void attackEnd(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time, Context context, SkillReport skillReport) {
        if (!effectState.getId().startsWith("ZHI_MING_JU_SHA")) {
            return;
        }
        final Addition addition = getAddition(passiveState);
        owner.increaseRate(UnitRate.CRIT, -addition.critProbUp);
        owner.increaseRate(UnitRate.CRIT_DAMAGE, -addition.critDmgRateUp);
    }

    private Addition getAddition(PassiveState passiveState) {
        Addition addition = passiveState.getAddition(Addition.class);
        if (addition == null) {
            addition = new Addition();
            passiveState.setAddition(addition);
        }
        return addition;
    }

    private static class Addition {
        private double critProbUp;
        private double critDmgRateUp;
    }

    @Override
    public PassiveType getType() {
        return PassiveType.ZHI_MING_JU_SHA;
    }
}
