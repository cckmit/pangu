package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.model.SkillType;
import lombok.Getter;

@Getter
public class AttackAddBuffsParam {
    //指定生效技能类型
    private SkillType type;

    //生效概率
    private double rate;

    //添加的buffID
    private String[] buffs;
}
