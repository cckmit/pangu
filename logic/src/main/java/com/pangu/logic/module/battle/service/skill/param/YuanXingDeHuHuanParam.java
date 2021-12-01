package com.pangu.logic.module.battle.service.skill.param;

import com.pangu.logic.module.battle.service.passive.param.BuffUpdateParam;
import lombok.Getter;
import lombok.Setter;

@Getter
public class YuanXingDeHuHuanParam {


    private double factor;

    private int reviveLimit;

    private String stoneBuffId;

    private int stoneRequire;

    @Setter
    private int zsMpAdd;

    @Setter
    private String zsBuffId;

    @Setter
    private int zsSelfMpAdd;

    private int reviveCount;


    private BuffUpdateParam stoneCounter;

}
