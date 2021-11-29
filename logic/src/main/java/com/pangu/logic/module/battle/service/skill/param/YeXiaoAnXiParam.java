package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

@Getter
public class YeXiaoAnXiParam {

    // 距离羽毛距离
    private int distance;

    // 物理伤害
    private double factor;

    // 是否消耗羽毛
    private boolean itemCost = true;
}
