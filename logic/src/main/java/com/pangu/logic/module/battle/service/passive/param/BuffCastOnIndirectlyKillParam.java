package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

import java.util.Map;

@Getter
public class BuffCastOnIndirectlyKillParam {
    /** 添加的<buffId,添加*/
    private Map<String,String> buff2ConExp;
    /** BUFF添加给谁*/
    private String target;

    /** 是否需要亲自收尾刀*/
    private boolean needDirectKill;
}
