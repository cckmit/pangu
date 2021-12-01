package com.pangu.logic.module.battle.service.skill.param;

import com.pangu.logic.module.battle.model.FilterType;
import com.pangu.logic.module.battle.service.buff.param.DefaultAddValueParam;
import com.pangu.logic.module.battle.service.passive.param.BuffUpdateParam;
import lombok.Getter;

import java.util.Map;

@Getter
public class EffectAreaParam {

    private DefaultAddValueParam valModParam;


    private StateAddParam stateAddParam;


    private BuffUpdateParam buff;


    private String anchorTargetId;

    private Map<Strategy, EnhancedAreaParam> strategy2BuildParam;

    public enum Strategy{

        TARGET_CIRCLE,

        BEST_CIRCLE,
    }


    private FilterType filter;
}
