package com.pangu.logic.module.battle.service.buff.param;

import lombok.Getter;

@Getter
public class DynamicRemoveOnceParam extends OnceParam {
    //  更新条件
    private String updateCond;
    //  移除条件
    private String removeCond;
    //  更新时修改的属性（一般为生命值、怒气，无需复位）
    private DefaultAddValueParam updateValModParam;
}
