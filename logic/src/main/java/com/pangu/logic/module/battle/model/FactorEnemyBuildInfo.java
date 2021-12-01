package com.pangu.logic.module.battle.model;

import lombok.Data;

@Data
public class FactorEnemyBuildInfo {

    private String enemyId;

    private double factor;

    public static FactorEnemyBuildInfo of(String enemyId,double factor){
        final FactorEnemyBuildInfo info = new FactorEnemyBuildInfo();
        info.enemyId = enemyId;
        info.factor = factor;
        return info;
    }
}
