package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

import java.util.List;

@Getter
public class DieUnitReduceDamageParam {
    //判断单元站位
    private List<Integer> sequence;

    //伤害减免比率
    private double rate;

    //是否需要死亡
    private boolean needDie;
}
