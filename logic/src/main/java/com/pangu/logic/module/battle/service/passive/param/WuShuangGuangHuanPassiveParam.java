package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class WuShuangGuangHuanPassiveParam {

    // 判断是否已经存在某个buff
    private String buffCheck;

    // 给自己添加buff
    private String selfAddBuff;

    // 恢复生命比率
    private double recoverHpRate;
}
