package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillReleasePassive;
import com.pangu.logic.module.battle.service.passive.param.JianDunParam;
import com.pangu.framework.utils.math.RandomUtils;
import org.springframework.stereotype.Component;

/**
 * 释放普攻时有概率为自己添加护盾
 */
@Component
public class JianDun implements SkillReleasePassive {

    @Override
    public PassiveType getType() {
        return PassiveType.JIAN_DUN;
    }

    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time, Context context, SkillReport skillReport) {
        if (owner != attacker) {
            return;
        }

        if (skillState.getType() != SkillType.NORMAL) {
            return;
        }

        final JianDunParam param = passiveState.getParam(JianDunParam.class);
        if (!RandomUtils.isHit(param.getTriggerRate())) {
            return;
        }

        final double shield = owner.getHighestATK() * param.getFactor();
        context.addPassiveValue(owner, AlterType.SHIELD_UPDATE, shield);
        final String id = owner.getId();
        skillReport.add(time, id, PassiveValue.single(passiveState.getId(), id, new UnitValues(AlterType.SHIELD_UPDATE, shield)));

        passiveState.addCD(time);
    }
}
