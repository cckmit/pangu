package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class YinXueParam {
    //饮血技能id，只有使用指定技能时才触发被动
    private String skillId;
    //恢复比例
    private double rate;
}
