package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

@Getter
public class ZhanZaiQianParam {

    /** 突进时长 */
    @Deprecated
    private int tujinTime;
    /** 伤害系数 */
    private double factor;
    /** 对同一个目标的突进间隔时间 */
    private int tujinInterval;
    /** 每次突进，提升伤害系数的百分比 */
    private double tujinDmgUpRate;
    /** 提升伤害系数的百分比上限 */
    private int tujinDmgUpRateLimit;
    /** 突进变范围伤害所需的突进次数 */
    private int rangeSelectTujinCount;
    /** 突进变范围攻击的选择标识（以目标为基准） */
    private String rangeSelectId;

}
