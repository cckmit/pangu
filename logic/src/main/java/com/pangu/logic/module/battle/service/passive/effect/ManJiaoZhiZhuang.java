package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.*;
import com.pangu.logic.module.battle.service.passive.param.ManJiaoZhiZhuangParam;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import com.pangu.framework.utils.math.RandomUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 蛮角之撞：
 * 受到伤害时，有20%概率获得暴怒BUFF，提升下一次平砍伤害50%
 * 2级：下一次平砍的伤害提升至100%
 * 3级：概率提升至35%
 * 4级：攻击伤害的80%会转化为一个护盾套在身上
 */
@Component
public class ManJiaoZhiZhuang implements DamagePassive, SkillSelectPassive, AttackPassive {
    @Override
    public PassiveType getType() {
        return PassiveType.MAN_JIAO_ZHI_ZHUANG;
    }

    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (owner == attacker) {
            return;
        }
        ManJiaoZhiZhuangParam param = passiveState.getParam(ManJiaoZhiZhuangParam.class);
        double rate = param.getRate();
        if (!RandomUtils.isHit(rate)) {
            return;
        }
        passiveState.setAddition(true);
    }

    @Override
    public SkillState skillSelect(PassiveState passiveState, SkillState skillState, Unit owner, int time) {
        if (skillState.getType() != SkillType.NORMAL) {
            return null;
        }

        Boolean addition = passiveState.getAddition(Boolean.class);
        if (addition == null || !addition) {
            return null;
        }
        passiveState.setAddition(null);

        ManJiaoZhiZhuangParam param = passiveState.getParam(ManJiaoZhiZhuangParam.class);
        String skillID = param.getSkillId();
        return SkillFactory.initState(skillID);
    }

    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        ManJiaoZhiZhuangParam param = passiveState.getParam(ManJiaoZhiZhuangParam.class);
        final String[] triggerSkills = param.getTriggerSkills();

        if (ArrayUtils.isEmpty(triggerSkills)) {
            return;
        }

        final String skillId = skillState.getId();
        if (Arrays.stream(triggerSkills).noneMatch(triggerSkill -> triggerSkill.equals(skillId))) {
            return;
        }

        final long shield = -(long) (param.getDmgToShieldRate() * damage);
        context.addPassiveValue(owner, AlterType.SHIELD_UPDATE, shield);
        skillReport.add(time, owner.getId(), PassiveValue.single(passiveState.getId(), owner.getId(), new UnitValues(AlterType.SHIELD_UPDATE, shield)));
    }
}
