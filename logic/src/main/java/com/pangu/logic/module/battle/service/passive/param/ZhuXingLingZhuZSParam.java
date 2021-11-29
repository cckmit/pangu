package com.pangu.logic.module.battle.service.passive.param;


import lombok.Getter;

@Getter
public class ZhuXingLingZhuZSParam {
    private double dmgUp;
    private double meteorDmgUp;
    private int meteorDeBuffDuration;
    private String meteorDeBuffId;
    private double meteorDefenceCutRatePerIceOrb;
    private int electricDeBuffDuration;
    private double electricNormalSpeedDownPerFireOrb;
    private double electricMpCutRatePerIceOrb;
    private String electricDeBuffId;
    private boolean consumeOrbs;
}
