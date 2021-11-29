package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class MoveWhenDmgParam {
    /**
     * 触发条件
     */
    private String triggerExp;

    /**
     * 移动到特定目标附近
     */
    private String targetPoint;

    /**
     * 特定目标发生移动
     */
    private String targetToMove;

    /**
     * 移动到特定位置，优先级比移动到特定目标附近要高，随攻守阵营偏移
     */
    private DestType destType = DestType.TARGET;

    public enum DestType {
        //  移动到目标身后
        TARGET,

        //  移动到我方的空旷角落
        EMPTY_SELF_CORNER
    }
}
