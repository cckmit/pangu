package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class BuffCastOnHpDownParam {
    //  生命百分比低于特定值时触发
    private double triggerHpPct;
    //  添加的buff
    private String buff;
}
