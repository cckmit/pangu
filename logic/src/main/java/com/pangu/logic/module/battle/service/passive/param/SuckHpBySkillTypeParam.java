package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.model.SkillType;
import lombok.Getter;

/**
 * 细粒度吸血
 */
@Getter
public class SuckHpBySkillTypeParam {
    private double rate;

    /** 触发该被动的技能类型*/
    private SkillType[] types;

    /** 生命值回复的目标（未必为自己）*/
    private String targetId = "SELF";
}
