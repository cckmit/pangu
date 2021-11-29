package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.ActiveSkillUpgradeParam;
import org.springframework.stereotype.Component;

/**
 * 将指定技能沿特定分支升级
 */
@Component
public class ActiveSkillUpgrade implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.ACTIVE_SKILL_UPGRADE;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final ActiveSkillUpgradeParam param = state.getParam(ActiveSkillUpgradeParam.class);
        if (param.getSkillTag() == null) {
            target.upgradeNormalSkill(param.getUpgradeIdx());
        } else {
            target.upgradeActiveSkillByTag(param.getSkillTag(), param.getUpgradeIdx());
        }
    }
}
