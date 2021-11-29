package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class ScheduledSkillActionGenerateParam {
    //<需要执行的技能ID，技能执行的时刻>
    private Map<String, List<Integer>> skillIdActionTimeMap;
}
