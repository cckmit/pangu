package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

import java.util.Collections;
import java.util.Set;

@Getter
public class SkillCdShareParam {

    private Set<String> skillIds = Collections.emptySet();

    private Set<String> skillTags = Collections.emptySet();

}
