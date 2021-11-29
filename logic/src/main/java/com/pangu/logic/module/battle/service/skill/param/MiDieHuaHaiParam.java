package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

@Getter
public class MiDieHuaHaiParam {

    private String[] execBuffIds;
    /** 移动倍率 */
    private double moveRate = 1.0;
    /** 添加BUFF所需的距离 */
    private int addBuffRange;

    private double rate;

    private String passiveId;

    private String buffId;

}
