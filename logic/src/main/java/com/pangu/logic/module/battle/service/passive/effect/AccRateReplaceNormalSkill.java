package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillSelectPassive;
import com.pangu.logic.module.battle.service.passive.param.AccRateReplaceNormalSkillParam;
import com.pangu.logic.module.battle.service.passive.param.AddBuffWhenAttackParam;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import com.pangu.framework.utils.math.RandomUtils;
import org.springframework.stereotype.Component;

@Component
public class AccRateReplaceNormalSkill implements SkillSelectPassive {
    @Override
    public PassiveType getType() {
        return PassiveType.ACC_RATE_REPLACE_NORMAL_SKILL;
    }

    @Override
    public SkillState skillSelect(PassiveState passiveState, SkillState skillState, Unit owner, int time) {
        if (skillState.getType() != SkillType.NORMAL) return null;
        final AccRateReplaceNormalSkillParam param = passiveState.getParam(AccRateReplaceNormalSkillParam.class);
        final double rateIncrease = param.getRateIncrease();
        final AdditionalParam addition = getAddition(passiveState);
        //触发后才执行逻辑
        final boolean triggered = RandomUtils.isHit(addition.acTriggerRate);
        if (!triggered) {
            //未触发累加补偿概率
            addition.acTriggerRate += rateIncrease;
            return null;
        } else {
            //触发重置概率
            addition.acTriggerRate = param.getBaseRate();
            passiveState.addCD(time);
            return SkillFactory.initState(param.getSkillId());
        }
    }

    private AdditionalParam getAddition(PassiveState passiveState) {
        AdditionalParam addition = passiveState.getAddition(AdditionalParam.class);
        if (addition == null) {
            addition = new AdditionalParam();
            //首次初始化时使用基础概率
            addition.acTriggerRate = passiveState.getParam(AccRateReplaceNormalSkillParam.class).getBaseRate();
            passiveState.setAddition(addition);
        }
        return addition;
    }

    private static class AdditionalParam {
        //用于统计累计触发率
        private double acTriggerRate;
    }
}
