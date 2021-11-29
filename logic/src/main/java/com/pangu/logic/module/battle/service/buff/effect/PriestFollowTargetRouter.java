package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.model.FilterType;
import com.pangu.logic.module.battle.service.buff.Buff;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 牧师跟随目标若处于不可选中状态，要通过该监听器重新选择跟随目标
 */
@Component
public class PriestFollowTargetRouter implements Buff {
    @Override
    public BuffType getType() {
        return BuffType.PRIEST_FOLLOW_TARGET_ROUTER;
    }

    @Override
    public void update(BuffState state, Unit unit, int time, Object addition) {
        final Unit traceUnit = unit.getTraceUnit();
        //当目标可选中时，无需进行任何操作
        if (traceUnit != null && traceUnit.canSelect(time)) {
            return;
        }
        //当不存在可选用的友方时，无需进行任何操作
        final List<Unit> friends = FilterType.FRIEND.filter(unit, time);
        int followableCount = 0;
        for (Unit friend : friends) {
            if (friend == unit) {
                continue;
            }
            if (friend.canSelect(time)) {
                followableCount++;
            }
        }
        if (followableCount == 0) {
            return;
        }

        //否则调用角色本身技能进行目标选择
        final String followInitSkill = state.getParam(String.class);
        SkillFactory.updateNextExecuteSkill(time, unit, followInitSkill);
    }
}
