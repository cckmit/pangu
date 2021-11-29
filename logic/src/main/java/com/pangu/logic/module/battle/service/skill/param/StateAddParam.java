package com.pangu.logic.module.battle.service.skill.param;

import com.pangu.logic.module.battle.model.UnitState;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StateAddParam {

    // 触发概率
    private double rate;

    // 状态类型
    private UnitState state;

    // 有效时间
    private int time;
}
