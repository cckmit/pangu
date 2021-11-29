package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

import java.util.List;

@Getter
public class DecreaseDamageFromSequenceParam {
    //受影响的位置信息
    private List<Integer> sequences;

    //伤害降低百分比
    private double rate;
}
