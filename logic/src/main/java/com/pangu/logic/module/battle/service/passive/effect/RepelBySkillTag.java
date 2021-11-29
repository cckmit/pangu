package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.model.report.values.PositionChange;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.RepelBySkillTagParam;
import com.pangu.logic.module.battle.service.skill.effect.Repel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 塔巴斯触发蛮角之撞时，将目标击退，且使其防御力降低20%
 */
@Component
public class RepelBySkillTag implements AttackPassive {
    @Autowired
    private Repel repel;

    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final RepelBySkillTagParam param = passiveState.getParam(RepelBySkillTagParam.class);
        if (!skillState.getTag().equals(param.getTag())) {
            return;
        }

        BuffFactory.addBuff(param.getBuff(), owner, target, time, skillReport, null);

        final PositionChange positionChange = new PositionChange();
        if (!repel.doRepel(param.getRepelDistance(), owner, target, time, positionChange)) {
            return;
        }
        skillReport.add(time, target.getId(), PassiveValue.single(passiveState.getId(), target.getId(), positionChange));
    }

    @Override
    public PassiveType getType() {
        return PassiveType.REPEL_BY_SKILL_TAG;
    }
}
