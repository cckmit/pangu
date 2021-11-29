package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class RecoverFriendAddMpParam {
    //添加多少mp
    private int addMp;

    //需要少于多少MP才添加
    private int lessMp;
}
