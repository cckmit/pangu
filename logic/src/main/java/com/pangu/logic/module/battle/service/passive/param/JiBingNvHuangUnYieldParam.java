package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.service.buff.param.DefaultAddValueParam;
import lombok.Getter;

@Getter
public class JiBingNvHuangUnYieldParam {
    //  触发的血线
    private double triggerHpPCT;

    //  修改参数
    private DefaultAddValueParam valModParam;

    //  为自己添加的状态
    private UnitState[] states;
    //  状态持续时间
    private int dur;
}
