package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class RepelBySkillTagParam{
    /** 击退距离*/
    private int repelDistance;

    /** 技能标签*/
    private String tag;

    /** 添加的BUFF*/
    private String buff;
}
