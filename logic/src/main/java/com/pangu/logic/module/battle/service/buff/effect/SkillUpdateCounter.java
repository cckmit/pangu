package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.buff.param.SkillUpdateCounterParam;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import org.springframework.stereotype.Component;


@Component
public class SkillUpdateCounter extends Counter {
    @Override
    protected void init() {
        super.setCallback(new Callback() {
            @Override
            public void exeWhenCountMax(BuffState state, Unit unit, int time) {
                SkillFactory.updateNextExecuteSkill(time,unit,state.getParam(SkillUpdateCounterParam.class).getSkillId());
            }
        });
    }

    @Override
    public BuffType getType() {
        return BuffType.SKILL_UPDATE_COUNTER;
    }
}
