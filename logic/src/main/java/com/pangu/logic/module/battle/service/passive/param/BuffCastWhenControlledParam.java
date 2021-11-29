package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.service.buff.param.DefaultAddValueParam;
import lombok.Getter;

@Getter
public class BuffCastWhenControlledParam {
    /** buff id*/
    private String buff;

    /** 触发概率*/
    private double triggerRate = 1;

    /** 属性修改*/
    private DefaultAddValueParam valModParam;

    /** 是否解除控制*/
    private boolean decontrol;
}
