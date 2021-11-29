package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.service.skill.param.StateAddParam;
import lombok.Getter;

@Getter
public class AddStateOnKillingParam {
    /** 添加状态*/
    private StateAddParam stateParam;
    /** 添加状态的目标*/
    private String target;

    /** 特定技能击杀*/
    private String skillTag;

    /** 需要被动持有者击杀*/
    private boolean selfKilling = true;
}
