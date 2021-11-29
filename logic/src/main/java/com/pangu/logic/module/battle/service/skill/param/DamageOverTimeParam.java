package com.pangu.logic.module.battle.service.skill.param;

import com.pangu.logic.module.battle.model.EffectType;
import lombok.Getter;

@Getter
public class DamageOverTimeParam {
    //伤害倍率
    private double factor;
    //生效间隔
    private int interval;
    //生效次数
    private int loops;
    //伤害类型（物理/魔法/真伤）
    private EffectType effectType;
}
