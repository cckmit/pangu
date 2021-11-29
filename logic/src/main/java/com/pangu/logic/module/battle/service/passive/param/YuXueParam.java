package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class YuXueParam {

    // 根据攻击力计算恢复血量
    private double recoverHpByAttackRate;

    // 增加怒气值
    private int addMp;
}
