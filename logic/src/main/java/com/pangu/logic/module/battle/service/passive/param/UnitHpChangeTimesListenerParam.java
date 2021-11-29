package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.service.buff.param.DefaultAddValueParam;
import lombok.Getter;

@Getter
public class UnitHpChangeTimesListenerParam {
    /** 触发行为所需次数*/
    private int triggerTimes;

    /** 是否监听友方*/
    private boolean listenFriend;
    /** 是否监听敌方*/
    private boolean listenEnemy;

    /** 属性调整参数*/
    private boolean modFriend;
    private boolean modEnemy;
    private DefaultAddValueParam valModParam;
}
