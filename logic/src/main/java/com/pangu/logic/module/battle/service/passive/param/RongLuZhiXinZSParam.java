package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.service.skill.param.HpRecoverParam;
import lombok.Getter;

@Getter
public class RongLuZhiXinZSParam {
    private double triggerHpPct;
    private String[] buffs;
    private HpRecoverParam hpRecoverParam;
}
