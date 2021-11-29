package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

@Getter
public class HuanXingNvShenZSParam {

    /** 复活的队友获得的能量值（专属装备） */
    private int zsMpAdd;
    /** 复活的队友获得的BUFF（专属装备） */
    private String zsBuffId;
    /** 成功复活时为自己回复的能量值（专属装备） */
    private int zsSelfMpAdd;
}
