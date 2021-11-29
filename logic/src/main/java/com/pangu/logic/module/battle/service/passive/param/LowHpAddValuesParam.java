package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.CalType;
import lombok.Getter;

import java.util.Map;

@Getter
public class LowHpAddValuesParam {
    //血量百分比
    private double hpRate;

    //是否血量高于当前比率否则低于
    private boolean greater;

    // 参数比率
    private double factor;

    // 计算方式
    private CalType calType;

    // 属性值
    private Map<AlterType, String> alters;
}
