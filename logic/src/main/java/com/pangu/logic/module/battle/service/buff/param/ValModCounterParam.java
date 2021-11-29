package com.pangu.logic.module.battle.service.buff.param;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.CalType;
import com.pangu.logic.module.battle.service.skill.param.StateAddParam;
import lombok.Getter;

import java.util.Map;

@Getter
public class ValModCounterParam extends CounterParam{
    /** 修改参数*/
    private CalType calType;
    private Map<AlterType,String> alters;
    private double factor;

    /** 添加异常*/
    private StateAddParam stateAddParam;
    private boolean needStateReport;
}
