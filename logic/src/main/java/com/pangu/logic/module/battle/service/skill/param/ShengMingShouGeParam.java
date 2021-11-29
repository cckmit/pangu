package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

@Getter
public class ShengMingShouGeParam {
    //最佳圆圈的半径
    private int r;
    //是否可被抵挡
    private boolean collide;
    //若能抵挡，弹道宽度
    private int width;
    //弹道最大射程
    private int length;
    //伤害参数
    private DamageParam dmg;
}
