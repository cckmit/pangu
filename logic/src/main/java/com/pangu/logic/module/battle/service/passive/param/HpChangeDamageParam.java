package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class HpChangeDamageParam {

    //每减少多少血量
    private double preHpDecrease;

    //提升多少输出
    private double increase;

    //最大提升多少
    private double max;

    //是否递减（为false 代表血量越低伤害越高 否则血量越高伤害越高）
    private boolean decrease;

    
    private boolean target;
}
