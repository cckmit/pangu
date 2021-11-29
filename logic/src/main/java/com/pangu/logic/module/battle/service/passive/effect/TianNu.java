package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillReleasePassive;
import com.pangu.logic.module.battle.service.passive.param.TianNuParam;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.skill.effect.HpMagicDamage;
import com.pangu.logic.module.battle.service.skill.effect.HpPhysicsDamage;
import com.pangu.framework.utils.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 释放大招有概率额外造成AOE伤害
 */
@Component
public class TianNu implements SkillReleasePassive {
    @Autowired
    private HpMagicDamage magicDamage;
    @Autowired
    private HpPhysicsDamage physicsDamage;

    @Override
    public PassiveType getType() {
        return PassiveType.TIAN_NU;
    }

    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time, Context context, SkillReport skillReport) {
        //仅自身释放大招时才造成额外伤害
        if (owner != attacker) {
            return;
        }
        if (skillState.getType() != SkillType.SPACE) {
            return;
        }

        //触发鉴定
        final TianNuParam param = passiveState.getParam(TianNuParam.class);
        if (!RandomUtils.isHit(param.getTriggerRate())) {
            return;
        }

        //取攻击力较高的一侧造成对应伤害
        final UnitValue atkValue = owner.getValue(UnitValue.ATTACK_M) > owner.getValue(UnitValue.ATTACK_P) ? UnitValue.ATTACK_M : UnitValue.ATTACK_P;
        final EffectState effectState = new EffectState(null, 0);
        effectState.setParamOverride(param);
        final List<Unit> targets = TargetSelector.select(owner, param.getTarget(), time);
        if (atkValue == UnitValue.ATTACK_M) {
            for (Unit target : targets) {
                PassiveUtils.hpMagicDamage(magicDamage, owner, target, null, effectState, time, context, skillReport, passiveState);
            }
        } else {
            for (Unit target : targets) {
                PassiveUtils.hpPhysicsDamage(physicsDamage, owner, target, null, effectState, time, context, skillReport, passiveState);
            }
        }

        passiveState.addCD(time);
    }
}
