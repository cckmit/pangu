package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class YinBaiZhiCiZSParam {

    /** 指定的技能标签 */
    private String skillTag;
    /** 恢复能量的点数 */
    private int cureMpValue;
    /** 按攻击力回复血量的比率 */
    private double cureHpRate;
    /** BUFF标识 */
    private String buffId;

}
