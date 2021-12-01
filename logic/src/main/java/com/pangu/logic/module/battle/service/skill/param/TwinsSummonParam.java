package com.pangu.logic.module.battle.service.skill.param;

import com.pangu.logic.module.battle.model.Point;
import lombok.Getter;

@Getter
public class TwinsSummonParam {

    private String skillPrefix;


    private String baseId;


    private int transformState;


    private Point point;


    private int removeTime;


    private int lastStrawDelay;


    private double rate;
}
