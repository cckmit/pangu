package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class WangLingGuWangZSParam {

    /** 指定的技能标签 */
    private String skillTag;
    /** BUFF标识 */
    private String buffId;
    /** 是否必定命中 */
    private boolean hit;
    /** 是否必定暴击 */
    private boolean crit;

}
