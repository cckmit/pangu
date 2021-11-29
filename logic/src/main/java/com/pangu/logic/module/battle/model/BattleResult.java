package com.pangu.logic.module.battle.model;

import com.pangu.framework.protocol.annotation.Transable;

/**
 * 战斗结果
 */
@Transable
public enum BattleResult {

    /**
     * 攻击方
     */
    ATTACKER,
    /**
     * 防守方
     */
    DEFENDER,
    /**
     * 超时
     */
    TIME_OUT,
    /**
     * 同归于尽
     */
    ALL_DEAD;

}
