package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class ZhanXingMoOuZSParam {
    //扣减目标能量
    private int mpCut;
    //回复自身能量
    private int mpRecover;
    //击杀额外回能
    private int mpRecoverWhenKill;
    //增伤比例
    private double dmgRate;
    //替换普攻
    private String replaceSkill;
}
