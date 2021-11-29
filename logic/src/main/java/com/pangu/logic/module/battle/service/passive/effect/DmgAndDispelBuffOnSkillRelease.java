package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.FilterType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.DispelType;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillReleasePassive;
import com.pangu.logic.module.battle.service.passive.param.DmgAndDispelBuffOnSkillReleaseParam;
import com.pangu.logic.module.battle.service.skill.effect.HpHigherDamage;
import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * "释放光之领域时将对范围内的敌人造成130%魔法攻击
 * 的伤害，并驱散范围内所有敌人的
 * 增益效果。
 * "
 */
@Component
public class DmgAndDispelBuffOnSkillRelease implements SkillReleasePassive {
    @Autowired
    private HpHigherDamage higherDamage;

    @Override
    public PassiveType getType() {
        return PassiveType.DMG_AND_DISPEL_BUFF_ON_SKILL_RELEASE;
    }

    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time, Context context, SkillReport skillReport) {
        final DmgAndDispelBuffOnSkillReleaseParam param = passiveState.getParam(DmgAndDispelBuffOnSkillReleaseParam.class);
        if (!skillState.getTag().startsWith(param.getTriggerSkillTag())) {
            return;
        }

        //  筛选目标
        final List<Unit> units = FilterType.ENEMY.filter(owner, time);
        final int r = param.getR();
        final DamageParam damageParam = param.getDamageParam();
        final String psvId = passiveState.getId();
        for (Unit unit : units) {
            if (unit.getPoint().distance(owner.getPoint()) <= r) {
                //  造成伤害
                context.passiveAtkDmg(owner, unit, time, skillReport, higherDamage, damageParam, psvId, null);
                //  移除增益
                BuffFactory.removeBuffsByDispelType(unit, time, DispelType.USEFUL);
            }
        }
    }
}
