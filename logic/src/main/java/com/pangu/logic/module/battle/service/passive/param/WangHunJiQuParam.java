package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.CalType;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class WangHunJiQuParam {

    private double factor;

    // 计算方式
    private CalType calType;

    // 属性值
    private Map<AlterType, String> alters;

}
