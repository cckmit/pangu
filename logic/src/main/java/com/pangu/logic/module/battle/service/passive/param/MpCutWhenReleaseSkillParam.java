package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.model.SkillType;
import lombok.Getter;

@Getter
public class MpCutWhenReleaseSkillParam {

    private double prob = 0.2;


    private long mpChange = -100;


    private SkillType[] skillType = new SkillType[]{SkillType.SPACE};


    private String targetId;
}
