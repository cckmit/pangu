package com.pangu.logic.module.battle.service.buff.param;

import com.pangu.logic.module.battle.service.skill.param.StateAddParam;
import lombok.Getter;

import java.util.Map;

@Getter
public class NeZhaZSParam {
    /** 火焰印记标签*/
    private String counterTag;

    /** 场上火焰印记总层数：已损失生命值回复率*/
    private Map<Integer,Double> totalCount2CureRate;

    /** 单人火焰印记层数到达指定数值后触发的异常*/
    private StateAddParam stateAddParam;
    /** 单人火焰印记层数到达指定数值后为目标添加的DeBuff*/
    private String deBuff;
    /** 单人火焰印记层数条件*/
    private int stateTriggerCount;
}
