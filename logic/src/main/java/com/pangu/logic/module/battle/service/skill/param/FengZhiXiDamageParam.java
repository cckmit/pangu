package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

@Getter
public class FengZhiXiDamageParam {

    /** 风之息BUFF标识 */
    private String buffId;
    /** 触发所需风之息BUFF个数 */
    private int triggerCount;
    /** 范围伤害的目标选择标识（已当前目标为基准） */
    private String targetRoundSelectId;
    /** 伤害系数 */
    private double factor;
    /** 击飞时间 */
    private int jifeiTime;

}
