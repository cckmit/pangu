package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.CalType;
import com.pangu.logic.module.battle.model.SkillType;
import lombok.Getter;

import java.util.Map;
import java.util.Set;

@Getter
public class AddValuesWhenSkillParam {


    private Set<SkillType> types;

    private double factor;

    private CalType calType;

    private Map<AlterType, String> alters;
}
