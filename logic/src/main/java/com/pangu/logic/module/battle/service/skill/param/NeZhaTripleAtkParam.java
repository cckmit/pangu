package com.pangu.logic.module.battle.service.skill.param;

import com.pangu.logic.module.battle.service.passive.param.BuffUpdateParam;
import lombok.Getter;

@Getter
public class NeZhaTripleAtkParam {
    /** 伤害参数，每次相同*/
    private DamageParam damageParam;

    /** 首次执行时更新火焰印记的层数*/
    private BuffUpdateParam buffUpdateParam;

    /** 第三次击飞时的异常参数*/
    private StateAddParam stateAddParam;

    /** 暴击奖励是否生效*/
    private boolean critBonus;
}
