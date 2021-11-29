package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

@Getter
public class BuildServantUnitParam {
    /** 施法柱模型ID*/
    private String baseId;
    /** 为施法柱添加的技能*/
    private String servantSkill;
    /** 在敌方场上构建施法柱*/
    private boolean buildForEnemy;
}


