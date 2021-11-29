package com.pangu.logic.module.battle.service.buff.param;

import lombok.Getter;

@Getter
public class RepeatParam extends DefaultAddValueParam{
    /** 更新次数上限  0:无限制*/
    private int updateLimit;

    /** 是否生成mark战报*/
    private boolean reportable;

    /** 到达更新上限后添加的增益*/
    private DefaultAddValueParam modValWhenReachLimit;
}
