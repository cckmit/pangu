package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

@Getter
public class ActiveSkillUpgradeParam {
    /** 需要升级的技能*/
    private String skillTag;

    /** 升级方向*/
    private int upgradeIdx;
}
