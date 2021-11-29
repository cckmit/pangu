package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

/**
 * 该参数对象提供额外的区域构建参数
 */
@Getter
public class EnhancedAreaParam extends AreaParam {
    /** 矩形长*/
    private int length;
    /** 矩形宽*/
    private int width;
}
