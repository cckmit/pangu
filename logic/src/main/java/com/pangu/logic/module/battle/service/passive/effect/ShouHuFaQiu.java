package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.model.report.values.PositionChange;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.ShouHuFaQiuParam;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.skill.effect.HpMagicDamage;
import com.pangu.logic.module.battle.service.skill.effect.Repel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 守护法球:
 * 受到伤害时击退周围敌方(范围内有敌方英雄则会触发),造成100%法攻伤害,触发间隔12秒
 * 2级:伤害提升至110%
 * 3级:击退效果翻倍,伤害提升至120%
 * 4级:触发时间降低为10秒
 */
@Component
public class ShouHuFaQiu implements DamagePassive {
    @Autowired
    private Repel repel;
    @Autowired
    private HpMagicDamage magicDamage;

    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (damage >= 0) {
            return;
        }
        final ShouHuFaQiuParam param = passiveState.getParam(ShouHuFaQiuParam.class);

        //筛选反击目标
        final List<Unit> targets = TargetSelector.select(owner, param.getTargetId(), time);

        //对目标造成伤害
        final EffectState effectState = new EffectState(null, 0);
        effectState.setParamOverride(param.getDamageParam());
        for (Unit target : targets) {
            PassiveUtils.hpMagicDamage(magicDamage, owner, target, skillState, effectState, time, context, skillReport, passiveState);
        }

        //击退目标
        final int repelDistance = param.getDistance();
        for (Unit target : targets) {
            final PositionChange positionReport = new PositionChange();
            final boolean repelSuccess = repel.doRepel(repelDistance, owner, target, time, positionReport);
            if (repelSuccess) {
                skillReport.add(time, target.getId(), PassiveValue.single(passiveState.getId(), owner.getId(), positionReport));
            }
        }

        passiveState.addCD(time);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.SHOU_HU_FA_QIU;
    }
}
