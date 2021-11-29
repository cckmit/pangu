package com.pangu.logic.module.battle.service.buff.param;

import com.pangu.logic.module.battle.model.UnitState;
import lombok.Getter;

import java.util.Map;

@Getter
public class GanDianGuangHuanParam extends CounterParam {
    //触发层数
    private int triggerCount;

    //添加状态
    private Map<UnitState, Integer> state;
}
