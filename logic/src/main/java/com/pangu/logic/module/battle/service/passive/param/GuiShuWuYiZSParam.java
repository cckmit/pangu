package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

import java.util.Collections;
import java.util.Set;

@Getter
public class GuiShuWuYiZSParam {


    private Set<String> skillTags = Collections.emptySet();

    private String unitId;

    private double hpRate;

    private double factor;

    private String selectId;

    private String cureBuffId;

    private int sneerTime;
}
