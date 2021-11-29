package com.pangu.logic.module.battle.service.skill.param;

import com.pangu.logic.module.battle.model.UnitState;
import lombok.Getter;


@Getter
public class KillParam {
    /** 复活系被动是否可免疫即死*/
    private boolean revivable;

    /**
     * 可免疫该效果的技能
     */
    private UnitState[] immuneStates = new UnitState[0];
}
