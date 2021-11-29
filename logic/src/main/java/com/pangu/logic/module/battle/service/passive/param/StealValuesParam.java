package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.CalType;
import lombok.Getter;

import java.util.Map;

@Getter
public class StealValuesParam {
    private int factor;

    //计算类型
    private CalType calType;

    //偷取/削弱的属性值
    private Map<AlterType, String> alters;

    //触发概率
    private double hitRate;

    //偷取需要满足的被动
    private String passiveId;
}
