package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Mp;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillReleasePassive;
import com.pangu.logic.module.battle.service.passive.SkillSelectPassive;
import com.pangu.logic.module.battle.service.passive.param.SiLieChangMaoZSParam;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import com.pangu.logic.module.battle.service.skill.effect.HpPhysicsDamage;
import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 撕裂长矛·特朗格尔专属装备
 * 1：第一次释放大招时，之后所有普通攻击的距离变为无限
 * 10：每次攻击暴击时会窃取敌人10点能量
 * 20：每次攻击暴击时会窃取敌人20点能量
 * 30：暴击的下一次普通攻击会额外攻击一名敌人，额外的攻击造成80%伤害
 * @author Kubby
 */
@Component
public class SiLieChangMaoZS implements SkillSelectPassive, SkillReleasePassive, AttackPassive {

    @Autowired
    private HpPhysicsDamage physicsDamage;

    @Override
    public PassiveType getType() {
        return PassiveType.SI_LIE_CHANG_MAO_ZS;
    }

    @Override
    public SkillState skillSelect(PassiveState passiveState, SkillState skillState, Unit owner, int time) {
        SiLieChangMaoZSParam param = passiveState.getParam(SiLieChangMaoZSParam.class);
        SiLieChangMaoZSAddition addition = passiveState
                .getAddition(SiLieChangMaoZSAddition.class, new SiLieChangMaoZSAddition());

        if (skillState.getType() == SkillType.SPACE) {
            if (addition.replaceNormalSkill == null) {
                addition.replaceNormalSkill = SkillFactory.initState(param.getReplaceSkillId());
            }
            return null;
        }

        if (skillState.getType() == SkillType.NORMAL) {
            return addition.replaceNormalSkill;
        }

        return null;
    }

    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time,
                             Context context, SkillReport skillReport) {
        if (owner != attacker) {
            return;
        }
        if (skillState.getType() != SkillType.NORMAL) {
            return;
        }
        SiLieChangMaoZSParam param = passiveState.getParam(SiLieChangMaoZSParam.class);
        if (param.getCritNormalFactor() <= 0) {
            return;
        }
        SiLieChangMaoZSAddition addition = passiveState
                .getAddition(SiLieChangMaoZSAddition.class, new SiLieChangMaoZSAddition());
        if (!addition.prevCrit) {
            return;
        }

        addition.prevCrit = false;

        List<Unit> extras = TargetSelector.select(owner, param.getCritNormalExtraSelectId(), time);

        EffectState effectState = new EffectState(null, 0);
        effectState.setParamOverride(new DamageParam(param.getCritNormalFactor()));

        for (Unit extra : extras) {
            physicsDamage.execute(effectState, owner, extra, skillReport, time, null, context);
        }
    }

    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context,
                       SkillState skillState, SkillReport skillReport) {
        if (!context.isCrit(target)) {
            return;
        }
        SiLieChangMaoZSParam param = passiveState.getParam(SiLieChangMaoZSParam.class);
        if (param.getCritMp() > 0) {
            int alterMP = param.getCritMp();
            context.addPassiveValue(owner, AlterType.MP, alterMP);
            context.addPassiveValue(target, AlterType.MP, -alterMP);
            skillReport.add(time, owner.getId(), PassiveValue.single(passiveState.getId(), owner.getId(), new Mp(alterMP)));
            skillReport.add(time, target.getId(), PassiveValue.single(passiveState.getId(), owner.getId(), new Mp(-alterMP)));
        }
        SiLieChangMaoZSAddition addition = passiveState
                .getAddition(SiLieChangMaoZSAddition.class, new SiLieChangMaoZSAddition());
        addition.prevCrit = true;
    }

    public class SiLieChangMaoZSAddition {

        SkillState replaceNormalSkill;

        boolean prevCrit;
    }
}
