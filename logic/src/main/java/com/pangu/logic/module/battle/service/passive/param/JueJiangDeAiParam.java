package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class JueJiangDeAiParam {

    // 百分比段
    private int[] percentRange;

    // 每个段达成条件后，增加的buff
    private String[] buffIds;
}
