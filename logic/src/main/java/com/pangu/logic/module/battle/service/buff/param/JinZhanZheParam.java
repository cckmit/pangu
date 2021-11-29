package com.pangu.logic.module.battle.service.buff.param;

import com.pangu.logic.module.battle.service.passive.param.BuffUpdateParam;
import lombok.Getter;

@Getter
public class JinZhanZheParam {
    //触发条件的选择器
    private String conditionTargetId;
    //被增强目标的选择器
    private String buffTargetId;
    //需要添加的buff
    private BuffUpdateParam buffWhenHasUnit;
    private BuffUpdateParam buffWhenNoUnit;

}
