package com.pangu.logic.module.battle.service.skill.param;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.CalType;
import lombok.Getter;

import java.util.Map;

@Getter
public class ValuesDamageParam {

    private double factor;
    // 计算方式
    private CalType calType;

    // 属性值
    private Map<AlterType, String> alters;
}
