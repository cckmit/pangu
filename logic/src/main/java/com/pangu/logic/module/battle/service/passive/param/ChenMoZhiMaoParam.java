package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.model.UnitState;
import lombok.Getter;

@Getter
public class ChenMoZhiMaoParam {
    //额外增伤比例
    private double damageUpRate;
    //技能触发概率
    private double triggerRate;
    //添加异常类型
    private UnitState stateType;
    //异常持续时间
    private int duration;

}
