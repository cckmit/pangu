package com.pangu.logic.module.battle.service.skill.param;

import com.pangu.logic.module.battle.model.EffectType;
import lombok.Getter;


/**
 * 更为通用的参数类型，封装技能效果、伤害参数、目标选择器
 */
@Getter
public class AttackParam {
    private EffectType attackType;
    private DamageParam dmg;
    private String targetId;
}
