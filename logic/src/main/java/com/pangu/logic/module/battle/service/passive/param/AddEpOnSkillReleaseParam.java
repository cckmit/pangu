package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

import java.util.Map;

@Getter
public class AddEpOnSkillReleaseParam {

    //<skillTag,EpAdd>
    private Map<String,Integer> tag2Ep;
}
