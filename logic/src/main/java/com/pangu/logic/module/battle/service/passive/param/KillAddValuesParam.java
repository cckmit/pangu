package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.CalType;
import lombok.Getter;

import java.util.Map;

@Getter
public class KillAddValuesParam {
    // 参数比率
    private double factor;

    // 计算方式
    private CalType calType;

    // 属性值
    private Map<AlterType, String> alters;

    // 被击杀的是召唤物，使用此参数
    private Map<AlterType, String> summonAlters;

    //是否需要自己击杀
    private boolean needSelf = true;

    //特定技能击杀
    private String triggerSkillTag;
}
