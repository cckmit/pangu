package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

@Getter
public class BuXiuLingYuParam {
    //区域参数
    private AreaParam area;
    //为敌方添加buff
    private String debuffId;
    //为友方添加buff
    private String buffId;
}
