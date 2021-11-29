package com.pangu.logic.module.battle.service.passive.param;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BuffUpdateParam {
    private String buffId;
    //更新延长时间
    private int delay;
    //true为重置策略，false为延长策略
    private boolean reset;
    //叠层类buff每次更新叠加的层数
    private int addition = 1;
}
