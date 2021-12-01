package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class DmgDelayParam {

    private double prob;

    private double triggerWhenHpPctLessThan = 0.7D;

    private int cd;

    private String buff;
}
