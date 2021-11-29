package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

@Getter
public class TransformParam {
    //变身后形态的标识
    private int baseId;
    //变身需要添加的被动
    private String[] addPassives;
    //变身需要移除的被动
    private String[] removePassives;
    //变身需要添加的buff
    private String[] addBuffs;
    //变身需要移除的buff
    private String[] removeBuffs;
    //变身持续时间
    private int duration;
    //变身后立即启动的技能
    private String skillId;

    //变身状态码
    private int state = 1;
}
