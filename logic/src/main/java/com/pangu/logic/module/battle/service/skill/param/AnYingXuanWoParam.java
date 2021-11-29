package com.pangu.logic.module.battle.service.skill.param;

import com.pangu.logic.module.battle.service.BattleConstant;
import lombok.Getter;

@Getter
public class AnYingXuanWoParam {

    // 旋涡每段吸引距离
    private int distance;

    // 拉扯位移终点距离漩涡中心的距离
    private int modifier = BattleConstant.SCOPE_HALF;

    // 目标选择器ID
    private String targetId;

    // 法术伤害比率
    private double factor;

    // 添加buff
    private String buff;
}
