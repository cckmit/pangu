package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.SkillCdShareParam;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

/**
 * 技能CD共享（不包含羁绊技能和初始化技能）
 * @author Kubby
 */
@Component
public class SkillCdShare implements SkillEffect {

    @Override
    public EffectType getType() {
        return EffectType.SKILL_CD_SHARE;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time,
                        SkillState skillState, Context context) {
        SkillCdShareParam param = state.getParam(SkillCdShareParam.class);

        List<SkillState> list = new LinkedList<>();
        for (SkillState ss : owner.getActiveSkills()) {
            if (param.getSkillIds().contains(ss.getId()) || param.getSkillTags().contains(ss.getTag())) {
                list.add(ss);
            }
        }

        SkillState skillState1 = null;
        SkillState skillState2 = null;
        if (list.size() == 2) {
            skillState1 = list.get(0);
            skillState2 = list.get(1);
        }

        if (skillState1 != null && skillState2 != null && skillState1 != skillState2) {
            skillState1.shareCdWith(skillState2);
        }
    }
}
