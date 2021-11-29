package com.pangu.logic.module.battle.service.skill.param;

import com.pangu.logic.module.battle.model.UnitState;
import lombok.Getter;


@Getter
public class DianCiZhaDanParam {
    //添加异常
    private UnitState unitState;
    //当目标身上有特定tag的buff时，才施加异常
    private String conditionTag;
    //异常持续时间
    private int duration;
    //目标选择
    private String targetId;
    //伤害倍率
    private double factor;

}
