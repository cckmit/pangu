package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

@Getter
public class HuaDiWeiLaoParam {

    /** 减伤被动标识 */
    private String passiveId;
    /** 眩晕BUFF标识 */
    @Deprecated
    private String buffId;

    /** 眩晕时间 */
    private int disableDuration = 5000;
}
