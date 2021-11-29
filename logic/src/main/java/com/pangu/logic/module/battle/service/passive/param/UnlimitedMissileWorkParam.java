package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class UnlimitedMissileWorkParam extends TimesReplaceNormalSkillParam{
    //  无限导弹触发次数
    private int unlimitedTriggerTimes;

    //  无限导弹触发次数存储的被动
    private String triggerPassive;
}
