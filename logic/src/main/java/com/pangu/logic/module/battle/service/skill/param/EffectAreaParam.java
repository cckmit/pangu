package com.pangu.logic.module.battle.service.skill.param;

import com.pangu.logic.module.battle.model.FilterType;
import com.pangu.logic.module.battle.service.buff.param.DefaultAddValueParam;
import com.pangu.logic.module.battle.service.passive.param.BuffUpdateParam;
import lombok.Getter;

import java.util.Map;

@Getter
public class EffectAreaParam {
    /** 属性修改*/
    private DefaultAddValueParam valModParam;

    /** 状态添加*/
    private StateAddParam stateAddParam;

    /** BUFF更新*/
    private BuffUpdateParam buff;

    /** 锚点目标选择器*/
    private String anchorTargetId;
    /** 区域选择策略*/
    private Map<Strategy, EnhancedAreaParam> strategy2BuildParam;

    public enum Strategy{
        /** 以目标选择器选出的目标为圆心构造多个圆形区域*/
        TARGET_CIRCLE,
        /** 以目标选择器选出的目标构造目标最密集的圆形区域*/
        BEST_CIRCLE,
    }

    /** 实际目标过滤器*/
    private FilterType filter;
}
