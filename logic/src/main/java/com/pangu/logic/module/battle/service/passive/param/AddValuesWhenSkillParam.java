package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.CalType;
import com.pangu.logic.module.battle.model.SkillType;
import lombok.Getter;

import java.util.Map;
import java.util.Set;

@Getter
public class AddValuesWhenSkillParam {

    /** 技能类型 */
    private Set<SkillType> types;
    /** 参数比率 */
    private double factor;
    /** 计算方式 */
    private CalType calType;
    /** 属性值 */
    private Map<AlterType, String> alters;
}
