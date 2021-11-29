package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.resource.FightSkillSetting;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.*;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import com.pangu.framework.resource.Storage;
import com.pangu.framework.resource.anno.Static;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 技能“致命狙杀”若是没有击杀目标，则会再次释放一次，每次释放必杀技最多触发一次
 */
@Component
public class SkillRepeatOnTargetSurvive implements AttackPassive, UnitDiePassive, SkillSelectPassive {
    @Static
    private Storage<String, FightSkillSetting> skillSettingStorage;

    @Override
    public PassiveType getType() {
        return PassiveType.SKILL_REPEAT_ON_TARGET_SURVIVE;
    }

    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> dieUnits) {
        if (owner != attacker) {
            return;
        }

        final SkillReport skillReport = damageReport instanceof SkillReport ? ((SkillReport) damageReport) : null;
        if (skillReport == null) {
            return;
        }

        final Addition addition = passiveState.getAddition(Addition.class, new Addition());
        if (addition.skillId == null) {
            return;
        }

        final String skillId = skillReport.getSkillId();
        final String skillTag = passiveState.getParam(String.class);
        if (!skillTag.equals(skillSettingStorage.get(skillId, true).getTag())) {
            return;
        }

        for (Unit dieUnit : dieUnits) {
            if (context.hasTag(dieUnit, skillId)) {
                addition.skillId = null;
                return;
            }
        }
    }

    private static String CONTEXT_ADD_KEY = PassiveType.SKILL_REPEAT_ON_TARGET_SURVIVE.name() + ":" + Phase.ATTACK.name() + ":MODIFIED";

    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final String skillTag = passiveState.getParam(String.class);
        if (!skillState.getTag().equals(skillTag)) {
            return;
        }

        final String skillId = skillState.getId();
        context.addTag(target, skillId);
        final Boolean modified = context.getAddition(CONTEXT_ADD_KEY, false);
        if (modified) {
            return;
        }
        final Addition addition = passiveState.getAddition(Addition.class, new Addition());
        addition.space = skillState.getType() == SkillType.SPACE;
        addition.skillId = addition.skillId == null ? skillId : null;
        context.setAddition(CONTEXT_ADD_KEY, true);
    }

    @Override
    public SkillState skillSelect(PassiveState passiveState, SkillState skillState, Unit owner, int time) {
        final Addition addition = passiveState.getAddition(Addition.class, new Addition());
        if (addition.skillId == null) {
            return null;
        }
        final SkillState replacingSkill = SkillFactory.initState(addition.skillId);
        if (addition.space) {
            owner.increaseValue(UnitValue.MP, owner.getValue(UnitValue.MP_MAX));
        }
        return replacingSkill;
    }

    private static class Addition {
        //  是否为大招
        private boolean space;
        //  技能id
        private String skillId;
    }
}
