package com.pangu.logic.module.battle.service.skill.param;

import com.pangu.logic.module.battle.model.UnitState;
import lombok.Getter;

@Getter
public class StateAddByTargetAmountParam {

    // 目标数量小于此数量时，触发此效果
    private int targetAmount;

    // 状态类型
    private UnitState state;

    // 有效时间
    private int time;
}
