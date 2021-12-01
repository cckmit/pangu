package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.service.skill.param.HpRecoverParam;
import lombok.Getter;

@Getter
public class RecoverOnControlOrEnemyDieParam {
    
    private int triggerTimes;

    
    private HpRecoverParam recParam;

    
    private int r;
}
