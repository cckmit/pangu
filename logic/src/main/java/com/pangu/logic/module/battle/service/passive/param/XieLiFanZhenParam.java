package com.pangu.logic.module.battle.service.passive.param;


import com.pangu.logic.module.battle.model.UnitState;
import lombok.Getter;

@Getter
public class XieLiFanZhenParam {
    //触发概率
    private double triggerRate;
    //伤害倍率
    private double factor;
    //添加异常
    private UnitState state;
    //异常持续时间
    private int duration;

    //必定触发的特殊处理
    private String triggerBuff;
}
