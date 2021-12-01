package com.pangu.logic.module.battle.service.buff.param;

import lombok.Getter;

@Getter
public class RepeatParam extends DefaultAddValueParam{

    private int updateLimit;


    private boolean reportable;


    private DefaultAddValueParam modValWhenReachLimit;
}
