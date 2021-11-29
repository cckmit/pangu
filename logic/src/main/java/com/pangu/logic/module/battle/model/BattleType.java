package com.pangu.logic.module.battle.model;

import com.pangu.framework.protocol.annotation.Transable;
import lombok.Getter;

/**
 * 战斗类型
 */
@Transable
public enum BattleType {
    /**
     * 常规战斗类型
     */
    NORMAL,

    /**
     * 竞技场
     */
    ARENA(true),

    /**
     * 王座之塔
     */
    KING_TOWER,

    /**
     * 远征
     */
    YUAN_ZHENG,

    /**
     * 翡翠幻境
     */
    DREAMLAND,

    /**
     * 冥渊神殿
     */
    MYSD,

    /**
     * 魔镜迷宫
     */
    MAZE,

    /**
     * 回溯之屋
     */
    HOUSE,

    /**
     * 高阶竞技场
     */
    HIGH_ARENA(true),

    /**
     * 蛮荒入侵
     */
    MHRQ,

    /**
     * 主线副本
     */
    SINGLE_BOSS,

    /**
     * 挂机战斗
     */
    AFK,

    /**
     * 节日BOSS
     */
    HOLIDAY_BOSS,

    /**
     * 据点争夺
     */
    STRONGHOLD,

    /**
     * 怪物扫荡
     */
    SWEEP_ENEMY,

    /**
     * 矿点争夺
     */
    MINERAL,

    /**
     * BOSS输出赛
     */
    CHALLENGE_BOSS,

    /**
     * 阵营战下半场
     */
    SECOND_HALF,

    /**
     * 突变BOSS
     */
    MUTATION,

    /**
     * 限时挑战
     */
    TIME_CHALLENGE,

    //奖励Boss
    REWARD_BOSS,

    //遗迹
    REMAINS,

    /**
     * 巅峰竞技场
     */
    PEAK_ARENA(true),

    /**
     * 玩家切磋
     */
    PLAYER_PK,

    /**
     * 收藏品
     */
    CABINET,
    /**
     * 1v1匹配
     */
    PVP,

    /**
     * 英雄试炼
     */
    HERO_CHALLENGE,

    /**
     * 公会讨伐
     */
    CORPS_BOSS,

    /**
     * 公会团战
     */
    CORPS_FIGHT,

    /**
     * 无尽试炼
     */
    ENDLESS_CHALLENGE,

    /**
     * 选择英雄试炼
     */
    SELECT_CHALLENGE,

    ;

    // 是否是PVP
    @Getter
    private boolean pvp;

    BattleType() {
    }

    BattleType(boolean pvp) {
        this.pvp = pvp;
    }
}
