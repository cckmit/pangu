package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.service.passive.PassiveType;
import lombok.Getter;

@Getter
public class ShenYuanMoLongParam {
    //被动延长时间
    private int delay;
    //替换普攻
    private String normalId;
    //替换技能
    private String skillId;
    //无视防御比例
    private double defenceIgnore;
    //给攻击目标添加debuff
    private String debuff;
}
