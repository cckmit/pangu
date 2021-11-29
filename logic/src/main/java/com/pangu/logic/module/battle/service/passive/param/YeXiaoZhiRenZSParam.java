package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

import java.util.Collections;
import java.util.Set;

@Getter
public class YeXiaoZhiRenZSParam {

    /** 会添加BUFF的技能标签 */
    private Set<String> skillTags = Collections.emptySet();
    /** BUFF标识 */
    private String buffId;

}
