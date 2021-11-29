package com.pangu.logic.module.battle.service.buff.param;

import lombok.Getter;

@Getter
public class SelfCircleCheckParam {

    // 范围半径
    private int radius;

    // 存在半径后添加buff
    private String buff;

    // 是否作用于队友
    private boolean friend;

    // 给自己添加一个被动
    private String passiveId;
}
