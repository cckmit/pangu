package com.pangu.logic.module.battle.service.buff.param;

import lombok.Getter;

@Getter
public class YueGuangZhiSuListenerParam {
    /** 聚焦时长，超时添加指定BUFF*/
    private int triggerTraceDuration;

    /** 为自身添加的增益*/
    private String buff;

    /** 为聚焦目标所添加的增益*/
    private String deBuff;

}
