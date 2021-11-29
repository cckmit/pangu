package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillSelectPassive;
import com.pangu.logic.module.battle.service.passive.param.CheckAndReplaceParam;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CheckAndReplace implements SkillSelectPassive {
    @Override
    public PassiveType getType() {
        return PassiveType.CHECK_AND_REPLACE;
    }

    @Override
    public SkillState skillSelect(PassiveState passiveState, SkillState skillState, Unit owner, int time) {
        if (skillState.getType() != SkillType.NORMAL) return null;
        final CheckAndReplaceParam param = passiveState.getParam(CheckAndReplaceParam.class);
        //计算指定区域内的单位数量
        final List<Unit> conditionUnits = TargetSelector.select(owner, param.getConditionTargetId(), time);
        //校验单位数量是否达成触发条件
        if (param.isHasUnit() && conditionUnits.size() <= 0) return null;
        if (!param.isHasUnit() && conditionUnits.size() > 0) return null;
        //通过校验替换技能
        return SkillFactory.initState(param.getSkillId());
    }
}
