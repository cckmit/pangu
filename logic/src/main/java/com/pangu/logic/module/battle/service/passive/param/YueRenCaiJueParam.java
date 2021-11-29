package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.service.skill.param.StateAddParam;
import lombok.Getter;

@Getter
public class YueRenCaiJueParam {
    private int stateAddTriggerCount;
    private int dmgUpTriggerCount;
    private int markResetTriggerCount;
    private StateAddParam state;
    private double dmgUpRate;
    private BuffUpdateParam buff;
}
