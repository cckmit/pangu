package com.pangu.logic.module.battle.model;

import com.pangu.framework.protocol.annotation.Transable;
import lombok.Getter;

/**
 * 战斗信息
 */
@Transable
@Getter
public class BattleInfo {

    /**
     * 战斗类型
     */
    private BattleType type;
    /**
     * 攻击方信息
     */
    private FighterInfo attacker;
    /**
     * 防守方信息
     */
    private FighterInfo defender;

    /**
     * 场景地图ID
     */
    private String sceneId;

    public static BattleInfo valueOf(BattleType type, FighterInfo attacker, FighterInfo defender, String sceneId) {
        BattleInfo result = new BattleInfo();
        result.type = type;
        result.attacker = attacker;
        result.defender = defender;
        result.sceneId = sceneId;
        return result;
    }

}
