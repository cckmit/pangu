package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class YueZhiShenPanParam {
    //触发该被动所需普攻次数
    private int triggerCount;

    //触发该被动时所添加或更新的buff
    private BuffUpdateParam buff;

    //触发该被动后所替换的技能
    private String skillId;

    //增伤率
    private double dmgUpRate;

    //普攻添加层数
    private int addCount;

    //暴击时总共（非额外）添加的印记数量
    private int addCountWhenCrit;

    //当队友死亡时为击杀者添加的印记数量
    private int addCountWhenFriendDie;
}
