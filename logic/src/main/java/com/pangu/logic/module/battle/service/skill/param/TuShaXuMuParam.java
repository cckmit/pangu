package com.pangu.logic.module.battle.service.skill.param;

import com.pangu.logic.module.battle.model.UnitState;
import lombok.Getter;

@Getter
public class TuShaXuMuParam {

    //伤害系数
    private double factor;

    //最后一次伤害表达式
    private String lastDamageExp;

    //眩晕参数
    private UnitState unitState = UnitState.DISABLE;
    private int stateDuration = 2000;
}
