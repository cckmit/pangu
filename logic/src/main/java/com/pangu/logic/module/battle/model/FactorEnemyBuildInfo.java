package com.pangu.logic.module.battle.model;

import lombok.Data;

@Data
public class FactorEnemyBuildInfo {
    /** EnemyFighterSettingId*/
    private String enemyId;
    /** 属性倍率*/
    private double factor;

    public static FactorEnemyBuildInfo of(String enemyId,double factor){
        final FactorEnemyBuildInfo info = new FactorEnemyBuildInfo();
        info.enemyId = enemyId;
        info.factor = factor;
        return info;
    }
}
