package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.model.UnitRate;
import lombok.Getter;

import java.util.Map;

@Getter
public class ZhiSiYiJiParam {

    // 触发概率
    private double rate;

    // 触发后增加属性，生效后移除
    private Map<UnitRate, Double> addRate;
}
