package com.pangu.logic.module.battle.service.buff.param;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.CalType;
import lombok.Getter;

import java.util.Map;

@Getter
public class CalValuesByTargetSizeParam {
    
    private String target;

    
    private CalType calType;
    
    private Map<AlterType,String> alters;

    private double factor;
}
