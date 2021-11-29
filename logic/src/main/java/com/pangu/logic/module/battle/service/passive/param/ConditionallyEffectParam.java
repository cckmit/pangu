package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.service.buff.param.DefaultAddValueParam;
import com.pangu.logic.module.battle.service.passive.Phase;
import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import com.pangu.logic.module.battle.service.skill.param.HpRecoverParam;
import com.pangu.logic.module.battle.service.skill.param.StateAddParam;
import lombok.Getter;

@Getter
public class ConditionallyEffectParam {
    /**
     * 触发阶段
     */
    private Phase phase;
    /**
     * 释放条件表达式
     */
    private String conExp;
    /**
     * 目标实施对象。
     */
    private String targetId;
    /**
     * 添加BUFF
     */
    private String buffId;
    /**
     * 修改数值
     */
    private DefaultAddValueParam valModParam;
    /**
     * 伤害参数
     */
    private DamageParam dmgParam;
    /**
     * 添加异常状态
     */
    private StateAddParam stateParam;
    /**
     * 治疗参数
     */
    private HpRecoverParam recoverParam;
    /**
     * 伤害修正系数
     */
    private double dmgCorrFactor;
}
