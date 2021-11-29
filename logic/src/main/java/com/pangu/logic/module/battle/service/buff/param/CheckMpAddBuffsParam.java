package com.pangu.logic.module.battle.service.buff.param;

import lombok.Getter;

@Getter
public class CheckMpAddBuffsParam {
    //需要大于的MP
    private int mp;

    //添加的BUFF
    private String[] buffs;
}
