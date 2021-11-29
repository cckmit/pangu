package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class HuDieXianZiZSParam {

    /** 指定的技能标签 */
    private String skillTag;
    /** 加BUFF的目标 */
    private String selectId;
    /** BUFF标识 */
    private String[] buffIds;

}
