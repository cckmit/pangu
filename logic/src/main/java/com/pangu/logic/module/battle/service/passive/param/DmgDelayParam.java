package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class DmgDelayParam {
    /** 触发概率*/
    private double prob;
    /** 当生命值低于该配置时才有概率触发*/
    private double triggerWhenHpPctLessThan = 0.7D;
    /** 触发cd*/
    private int cd;
    /** 用于引爆伤害的定时炸弹*/
    private String buff;
}
