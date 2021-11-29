package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class WuJinShaLuParam {
    //斩杀线
    private double slayHpPct;
    //斩杀伤害倍率
    private double slayDmgRate;

    //触发该被动技能标签
    private String triggerSkillTag;
}
