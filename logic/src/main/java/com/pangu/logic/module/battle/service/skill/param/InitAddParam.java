package com.pangu.logic.module.battle.service.skill.param;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.CalType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 添加属性或者buff或者被动
 */
@NoArgsConstructor
@Getter
public class InitAddParam {
    // 计算方式
    private CalType calType;

    private double factor;
    // 属性值
    private Map<AlterType, String> alters;

    //添加的Buff
    private String[] buffs;

    //添加的被动
    private String[] passives;

    //触发需要的血量
    private Double hpCondition;
}
