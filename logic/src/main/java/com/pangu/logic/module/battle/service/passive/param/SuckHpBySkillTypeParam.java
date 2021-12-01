package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.model.SkillType;
import lombok.Getter;

/**
 * 细粒度吸血
 */
@Getter
public class SuckHpBySkillTypeParam {
    private double rate;


    private SkillType[] types;


    private String targetId = "SELF";
}
