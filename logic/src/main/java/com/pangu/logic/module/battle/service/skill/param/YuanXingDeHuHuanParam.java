package com.pangu.logic.module.battle.service.skill.param;

import com.pangu.logic.module.battle.service.passive.param.BuffUpdateParam;
import lombok.Getter;
import lombok.Setter;

@Getter
public class YuanXingDeHuHuanParam {

    /** 复活血量系数 */
    private double factor;
    /** 复活次数上限 */
    private int reviveLimit;
    /** 神灵晶石BUFF标签 */
    private String stoneBuffId;
    /** 复活队友所需的神灵晶石BUFF个数 */
    private int stoneRequire;
    /** 复活的队友获得的能量值（专属装备） */
    @Setter
    private int zsMpAdd;
    /** 复活的队友获得的BUFF（专属装备） */
    @Setter
    private String zsBuffId;
    /** 成功复活时为自己回复的能量值（专属装备） */
    @Setter
    private int zsSelfMpAdd;
    /** 复活人数*/
    private int reviveCount;

    /** 石头计数器buff*/
    private BuffUpdateParam stoneCounter;

}
