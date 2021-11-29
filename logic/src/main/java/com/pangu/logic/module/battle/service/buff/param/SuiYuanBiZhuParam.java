package com.pangu.logic.module.battle.service.buff.param;

import com.pangu.logic.module.battle.service.skill.param.StateAddParam;
import lombok.Getter;


@Getter
public class SuiYuanBiZhuParam {
    //距离buff施法者一定距离内添加异常
    private StateAddParam stateAddParam;
    //距离buff施法者一定距离外添加deBuff
    private String buffId;
    //距离施法者的距离。大于和小于该距离分别触发不同效果
    private int border;
    //触发条件参数
    private double triggerAccumNormalAtkChangeRate;
}
