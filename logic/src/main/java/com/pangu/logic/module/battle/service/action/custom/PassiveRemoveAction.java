package com.pangu.logic.module.battle.service.action.custom;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.action.Action;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import lombok.Data;


/**
 * 用于及时移除被动
 */
@Data
public class PassiveRemoveAction implements Action {
    private int time;
    private final Unit owner;
    private final PassiveState passiveState;
    private final SkillReport skillReport;

    @Override
    public void execute() {
        if (passiveState.shouldRemove(time)) {
            owner.removePassive(passiveState);
        } else {
            time = passiveState.getTime();
            owner.addTimedAction(this);
        }
    }
}
