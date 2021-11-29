package com.pangu.logic.module.battle.facade;

import com.pangu.framework.socket.anno.SocketDefine;

/**
 * 战斗模块定义信息
 */
@SocketDefine
public interface BattleModule {

    /**
     * 当前的模块标识(13)
     */
    short MODULE = 13;

    // 指令值定义部分

    /**
     * FightUnitSetting#id 构建
     */
    int COMMAND_TEST = 1;

    /**
     * 战斗单元构建
     */
    int COMMAND_TEST_UNIT = 2;

    /**
     * 根据两个战斗单元ID构建
     */
    int COMMAND_TEST_ID = 3;
}
