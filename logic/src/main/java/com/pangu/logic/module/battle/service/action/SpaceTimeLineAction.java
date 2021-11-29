package com.pangu.logic.module.battle.service.action;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.service.Battle;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;

import java.util.ArrayDeque;
import java.util.List;

/**
 * 世界时钟检测大招CD
 * 如果CD到达，直接执行大招队列中存在的大招
 */
public class SpaceTimeLineAction implements Action {

    // 检测时间点
    private int time;

    // 战场引用
    private Battle battle;

    public SpaceTimeLineAction(int time, Battle battle) {
        this.time = time;
        this.battle = battle;
    }

    @Override
    public int getTime() {
        return time;
    }

    @Override
    public void execute() {
        ArrayDeque<Unit> spaceValidUnits = battle.getSpaceValidUnits();
        OUTER:
        for (Unit unit : spaceValidUnits) {
            Action action = unit.getAction();
            if (!(action instanceof SkillAction)) {
                continue;
            }
            SkillState skillState = ((SkillAction) action).getSkillState();
            if (skillState.getType() != SkillType.NORMAL) {
                continue;
            }
            List<SkillState> activeSkills = unit.getActiveSkills();
            for (SkillState skill : activeSkills) {
                if (skill.getType() == SkillType.SPACE && !skill.isValid(time, unit)) {
                    continue OUTER;
                }
            }
            ((SkillAction) action).broken(time);
            battle.removeSpaceValidUnits(unit);
            break;
        }
    }
}
