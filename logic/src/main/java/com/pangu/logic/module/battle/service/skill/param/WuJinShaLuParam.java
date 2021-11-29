package com.pangu.logic.module.battle.service.skill.param;

import com.pangu.logic.module.battle.model.UnitRate;
import com.pangu.logic.module.battle.model.UnitValue;
import lombok.Getter;

import java.util.Map;

@Getter
@Deprecated
public class WuJinShaLuParam {
    // 攻击比率参数
    private double factor;

    // 暴击触发公式
    private String critExp;

    // 目标生命低于百分比
    private int hpRateDamageDeepenPercent;

    // 目标生命低于百分比，伤害加深倍数
    private int hpRateDamageDeepen;

    // 个人提升
    private Map<UnitRate, Double> ownerAddRate;

    // 直接修改目标的属性值到某个百分比
    private Map<UnitValue, Double> targetValueChangeRate;

    // 使用一次的被动
    private String passiveId;
}
