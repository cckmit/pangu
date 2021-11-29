package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.CalType;
import lombok.Data;

import java.util.Map;

@Data
public class InitValuesParam {

    private double factor;
    // 计算方式
    private CalType calType;

    // 属性值
    private Map<AlterType, String> alters;
}
