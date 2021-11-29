package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

@Getter
public class TwinsRangedSpaceParam {
    /** 对沉默单位增伤率*/
    private double dmgRate;
    /** 触发增伤所需EP*/
    private int dmgEp;

    /** 能量降低率*/
    private double mpCutPct;
    /** 触发能量降低所需EP*/
    private int mpCutEp;

    /** 牵引距离*/
    private int dragDist;
    /** 触发牵引所需EP*/
    private int dragEp;
    /** 牵引状态持续时间*/
    private int dragDur;

    /** 伤害参数*/
    private DamageParam dmgParam;
}
