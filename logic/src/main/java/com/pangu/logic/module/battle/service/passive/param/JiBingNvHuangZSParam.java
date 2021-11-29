package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.service.skill.param.StateAddParam;
import lombok.Getter;

@Getter
public class JiBingNvHuangZSParam {
    private double triggerProb;
    private double triggerProbWhenAttackSummonUnit;
    private double dmgUpWhenAttackSummonUnit;
    private double mpRecoverWhenKillSummonUnit;
    private StateAddParam state;
}
