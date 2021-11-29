package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.service.skill.param.HpRecoverParam;
import lombok.Getter;

@Getter
public class RecoverOnControlOrEnemyDieParam {
    /** 控制或阵亡人数到达该数字后才能触发*/
    private int triggerTimes;

    /** 回复参数*/
    private HpRecoverParam recParam;

    /** 指定半径内死亡的敌人才计数*/
    private int r;
}
