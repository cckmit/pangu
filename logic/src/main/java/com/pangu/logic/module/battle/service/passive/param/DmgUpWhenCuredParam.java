package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class DmgUpWhenCuredParam {
    /** 每累计获得 curedHpPct 最大生命值的治疗*/
    private double curedHpPct;
    /** 提升 dmgUpRate 的伤害*/
    private double dmgUpRate;
}
