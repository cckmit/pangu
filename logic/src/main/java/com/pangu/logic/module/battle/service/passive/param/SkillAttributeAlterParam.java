package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.model.CalType;
import com.pangu.logic.module.battle.model.UnitRate;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.service.passive.Phase;
import lombok.Getter;

import java.util.Collections;
import java.util.Map;

@Getter
public class SkillAttributeAlterParam {


    private Phase phase;

    private boolean alterOwner;

    private String condition;

    private CalType calType;

    private Map<UnitValue, String> values = Collections.emptyMap();

    private Map<UnitRate, String> rates = Collections.emptyMap();

}
