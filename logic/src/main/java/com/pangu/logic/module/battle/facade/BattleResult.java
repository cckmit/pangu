package com.pangu.logic.module.battle.facade;

import com.pangu.framework.protocol.annotation.Constant;
import com.pangu.framework.utils.model.ResultCode;

/**
 * 战斗模块返回状态码定义
 */
@Constant
public interface BattleResult extends ResultCode {

    /**
     * 配置信息错误
     */
    int CONFIG_ERROR = -1300;

    /**
     * 参数信息错误
     */
    int ARGUMENT_ERROR = -1301;

    /**
     * 战斗不存在
     */
    int BATTLE_NOT_FOUND = -1302;

    /**
     * 战斗状态错误
     */
    int STATE_ERROR = -1303;

    /**
     * 选择的技能不存在
     */
    int SKILL_NOT_FOUND = -1304;

    /**
     * 技能无法选中
     */
    int SKILL_CANNOT_CHOSE = -1305;

    /**
     * 战斗不能不等待及时返回战报
     */
    int BATTLE_CANNOT_NOWAIT = -1306;

    /**
     * 攻击方战斗单位无法构建
     */
    int ATTACKER_NOT_FOUND = -1307;

    /**
     * 防守方战斗单位无法构建
     */
    int DEFENDER_NOT_FOUND = -1308;

    /**
     * 玩家已经在战斗中
     */
    int PLAYER_ALREADY_IN_BATTLE = -1309;

    /**
     * 战斗过程中发生错误
     */
    int PROCESS_ERROR = -1310;

    /**
     * 战斗存在没有死亡的单位
     */
    int UNIT_NOT_DEAD = -1311;

}
