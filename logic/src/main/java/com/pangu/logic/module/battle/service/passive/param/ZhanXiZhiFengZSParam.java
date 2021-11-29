package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class ZhanXiZhiFengZSParam {

    /** 斩在前技能标签 */
    private String skillTag;
    /** BUFF标识 */
    private String buffId;
    /** 叠加上限 */
    private int overlayLimit;

}
