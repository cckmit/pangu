package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillSelectPassive;
import com.pangu.logic.module.battle.service.passive.param.SkillReplaceByIdPrefixParam;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import com.pangu.logic.utils.ExpressionHelper;
import com.pangu.framework.utils.reflect.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 根据技能id前缀替换技能，通用于特殊培养模块
 */
@Component
public class SkillReplaceByIdPrefix implements SkillSelectPassive {
    @Override
    public PassiveType getType() {
        return PassiveType.SKILL_REPLACE_BY_ID_PREFIX;
    }

    @Override
    public SkillState skillSelect(PassiveState passiveState, SkillState skillState, Unit owner, int time) {
        final SkillReplaceByIdPrefixParam param = passiveState.getParam(SkillReplaceByIdPrefixParam.class);
        if (!StringUtils.isEmpty(param.getCond())) {
            final HashMap<String, Object> ctx = new HashMap<>(4, 1);
            ctx.put("owner", owner);
            ctx.put("time", time);
            ctx.put("skillState", skillState);
            ctx.put("random", ThreadLocalRandom.current());
            if (!ExpressionHelper.invoke(param.getCond(), boolean.class, ctx)) {
                return null;
            }
        }

        String replacedId = param.replace(skillState.getId());
        if (replacedId != null) {
            return SkillFactory.initState(replacedId);
        }
        for (SkillState activeSkill : owner.getActiveSkills()) {
            replacedId = param.replace(activeSkill.getId());
            if (replacedId != null) {
                return SkillFactory.initState(replacedId);
            }
        }
        return null;
    }
}
