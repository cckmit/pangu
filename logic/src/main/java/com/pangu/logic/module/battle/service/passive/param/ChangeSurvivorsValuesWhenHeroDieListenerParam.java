package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.CalType;
import lombok.Getter;

import java.util.Map;

@Getter
public class ChangeSurvivorsValuesWhenHeroDieListenerParam {
    /**
     * 属性修改参数
     */
    private double factor;
    private CalType calType;
    private Map<AlterType, String> alters;

    /**
     * 监听友方
     */
    private boolean friend;
}
