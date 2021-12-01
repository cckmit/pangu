package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.service.buff.param.DefaultAddValueParam;
import lombok.Getter;

@Getter
public class UnitHpChangeTimesListenerParam {
    
    private int triggerTimes;

    
    private boolean listenFriend;
    
    private boolean listenEnemy;

    
    private boolean modFriend;
    private boolean modEnemy;
    private DefaultAddValueParam valModParam;
}
