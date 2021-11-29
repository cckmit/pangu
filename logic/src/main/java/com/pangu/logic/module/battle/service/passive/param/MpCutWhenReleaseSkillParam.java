package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.model.SkillType;
import lombok.Getter;

@Getter
public class MpCutWhenReleaseSkillParam {
    /** 触发概率*/
    private double prob = 0.2;

    /** 能量扣减*/
    private long mpChange = -100;

    /** 可触发此被动的技能类型*/
    private SkillType[] skillType = new SkillType[]{SkillType.SPACE};

    /** 被扣减能量的目标*/
    private String targetId;
}
