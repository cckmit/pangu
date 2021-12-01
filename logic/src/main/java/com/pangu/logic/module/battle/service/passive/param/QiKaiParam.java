package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class QiKaiParam {


    private double rate;

    private double damagePct;

    private String selectId;

    private double dmgLimit;


    private int triggerNormalAtkTimes = Integer.MAX_VALUE;


    private String deBuff;


    private int deBuffLimit;
}
