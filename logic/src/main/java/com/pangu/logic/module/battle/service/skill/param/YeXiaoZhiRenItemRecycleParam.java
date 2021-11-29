package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

@Getter
public class YeXiaoZhiRenItemRecycleParam {
    /**
     * 回收道具路经的弹道的宽度
     */
    private int width;

    /**
     * 同一目标被弹道多次命中会有异常状态
     */
    private int stateTriggerHitCount;

    /**
     * 添加的异常状态
     */
    private StateAddParam stateParam;

    /**
     * 每个道具造成的伤害
     */
    private DamageParam dmgParam;
}
