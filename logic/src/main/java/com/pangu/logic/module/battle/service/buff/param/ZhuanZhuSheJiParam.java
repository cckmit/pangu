package com.pangu.logic.module.battle.service.buff.param;

import lombok.Getter;

@Getter
public class ZhuanZhuSheJiParam {
    //触发条件的选择器
    private String conditionTargetId;
    //被增强目标的选择器
    private String buffTargetId;
    //被添加的buff
    private String buffId;
    //被添加的被动
    private String passiveId;
}
