package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class BuffCastWhenBeCritParam {
    /** 最大可叠加BUFF数量*/
    private int maxCount;
    /** 被暴击时添加的BUFF_ID*/
    private String buffId;
}
