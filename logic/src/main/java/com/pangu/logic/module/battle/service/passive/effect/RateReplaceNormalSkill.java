package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillSelectPassive;
import com.pangu.logic.module.battle.service.passive.param.RateReplaceNormalSkillParam;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import com.pangu.framework.utils.math.RandomUtils;
import org.springframework.stereotype.Component;

/**
 * 狮皇之影
 * 每次普通攻击时有20%的概率造成一次普通攻击的连击效果（无法暴击）
 * 2级：概率提升至30%
 * 3级：连击效果无视对方20%的护甲
 * 4级：连击效果可以造成暴击
 */
@Component
public class RateReplaceNormalSkill implements SkillSelectPassive {

    @Override
    public SkillState skillSelect(PassiveState passiveState, SkillState skillState, Unit owner, int time) {
        if (skillState.getType() != SkillType.NORMAL) {
            return null;
        }
        RateReplaceNormalSkillParam param = passiveState.getParam(RateReplaceNormalSkillParam.class);
        if (!RandomUtils.isHit(param.getRate())) {
            return null;
        }
        passiveState.addCD(time);
        return SkillFactory.initState(param.getSkillId());
    }

    @Override
    public PassiveType getType() {
        return PassiveType.RATE_REPLACE_NORMAL_SKILL;
    }
}
