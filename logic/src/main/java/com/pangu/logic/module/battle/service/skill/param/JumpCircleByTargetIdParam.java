package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

@Getter
public class JumpCircleByTargetIdParam {
    //目标筛选器
    private String targetId;

    //最佳圆圈的半径
    private int radius;
}
