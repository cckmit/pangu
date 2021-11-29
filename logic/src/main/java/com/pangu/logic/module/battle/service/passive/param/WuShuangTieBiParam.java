package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class WuShuangTieBiParam {

    // 开始生效技能标签
    private String startSkillTag;

    // 停止生效技能标签
    private String endSkillTag;

    // 生效后给自己添加的buff
    private String buff;

    // 减伤比率
    private double reduceDamageRate;

    // 反伤比率
    private double reflectRate;
}
