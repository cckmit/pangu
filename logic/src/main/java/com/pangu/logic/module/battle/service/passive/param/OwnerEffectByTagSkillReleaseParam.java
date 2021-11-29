package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.service.buff.param.DefaultAddValueParam;
import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import com.pangu.logic.module.battle.service.skill.param.StateAddParam;
import lombok.Getter;

@Getter
public class OwnerEffectByTagSkillReleaseParam {
    /** 技能标签*/
    private String skillTag;

    /** 触发概率*/
    private double rate;

    /** 目标实施对象*/
    private String targetId;

    /** 添加BUFF*/
    private String buffId;

    /** 修改数值*/
    private DefaultAddValueParam valModParam;

    /** 造成伤害*/
    private DamageParam dmgParam;

    /** 击退距离*/
    private int repelDist;

    /** 添加状态*/
    private StateAddParam stateAddParam;

    /** 添加被动*/
    private String passive;
}
