package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import lombok.Getter;

@Getter
public class DmgAndDispelBuffOnSkillReleaseParam {
    /**
     * 触发此被动的技能标签
     */
    private String triggerSkillTag;

    /**
     * 伤害系数
     */
    private DamageParam damageParam;

    /** 作用半径*/
    private int r;
}
