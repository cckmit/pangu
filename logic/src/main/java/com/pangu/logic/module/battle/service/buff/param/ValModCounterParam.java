package com.pangu.logic.module.battle.service.buff.param;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.CalType;
import com.pangu.logic.module.battle.service.skill.param.StateAddParam;
import lombok.Getter;

import java.util.Map;

@Getter
public class ValModCounterParam extends CounterParam{
    
    private CalType calType;
    private Map<AlterType,String> alters;
    private double factor;

    
    private StateAddParam stateAddParam;
    private boolean needStateReport;
}
