package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.FilterType;
import com.pangu.logic.module.battle.model.SelectType;
import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.UnitType;
import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillSelectPassive;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import org.springframework.stereotype.Component;

@Component
public class MoRiShenPanRouter implements SkillSelectPassive {
    @Override
    public PassiveType getType() {
        return PassiveType.MO_RI_SHEN_PAN_ROUTER;
    }

    @Override
    public SkillState skillSelect(PassiveState passiveState, SkillState skillState, Unit owner, int time) {
        //只替换大招
        if (skillState.getType() != SkillType.SPACE) {
            return null;
        }
        //根据敌方阵容判断是否要替换技能
        final SelectSetting intellectSelectSetting = SelectSetting.builder()
                .filter(FilterType.ENEMY)
                .selectType(SelectType.UNIT_TYPE)
                .realParam(UnitType.INTELLECT).build();
        final double intellectEnemiesCount = TargetSelector.select(owner, time, intellectSelectSetting).size();
        final double enemiesCount = FilterType.ENEMY.filter(owner, time).size();
        final String skill = passiveState.getParam(String.class);
        if (intellectEnemiesCount / enemiesCount < 0.5) {
            return null;
        }
        return SkillFactory.initState(skill);
    }
}
