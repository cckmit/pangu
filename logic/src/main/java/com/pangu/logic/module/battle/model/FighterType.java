package com.pangu.logic.module.battle.model;

import com.pangu.framework.protocol.annotation.Transable;

/**
 * 战斗单位类型
 */
@Transable
public enum FighterType {

    //  配置敌军的战斗单位
    ENEMY,

    //  配置玩家
    PLAYER,

    // 战斗单元
    ENEMY_UNIT,

    // 单个战斗单元ID
    ENEMY_UNIT_ID,

    //根据FormationBuildInfo
    FORMATION_INFO,

    // 根据UnitBuildInfo构建
    @Deprecated
    BUILDER_INFO,

    // 根据UnitBuildInfos构建
    FORMATION_INFOS,

    //固定等级构建
    FIX_LEVEL,

    // 基于怪物表乘以一定系数的构建
    FACTOR_ENEMY,
    ;
}
