package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class FengZhiXiParam {

    /** 替换的技能标识 */
    private String skillId;
    /** 风之息BUFF标识 */
    private String buffId;
    /** 替换技能所需的风之息BUFF层数 */
    private int overlayCount;
    /** 添加风之息BUFF的概率 */
    private double rate;

}
