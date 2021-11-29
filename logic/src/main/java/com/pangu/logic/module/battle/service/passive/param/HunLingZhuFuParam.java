package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class HunLingZhuFuParam {

    // 地方每恢复多少百分比自己恢复生命
    private int enemyRecoverPercent;

    // 自己恢复血量百分比
    private double recoverRate;
}
