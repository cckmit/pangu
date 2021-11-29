package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

@Getter
public class PassiveReplaceParam {
    //  被替换技能ID的前缀
    private String replacedPrefix;

    //  替换方的前缀
    private String replacingPrefix;
}
