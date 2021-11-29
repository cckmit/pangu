package com.pangu.logic.module.battle.service.buff.param;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.CalType;
import lombok.Getter;

import java.util.Map;

@Getter
public class DecreaseValuesParam {
    // 参数比率
    private double factor;

    // 计算方式
    private CalType calType;

    // 初始化属性值
    private Map<AlterType, String> initValues;

    // 递减属性值
    private Map<AlterType, String> decreaseValue;
}
