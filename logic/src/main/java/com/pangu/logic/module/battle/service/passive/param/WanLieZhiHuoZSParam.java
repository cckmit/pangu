package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class WanLieZhiHuoZSParam {

    /** 点燃分类标识 */
    private String burnClassify;
    /** 普攻暴击时添加的BUFF标识 */
    private String buffId;
    /** 受到被点燃的单位的攻击时的伤害降低率 */
    private double rate;

}
