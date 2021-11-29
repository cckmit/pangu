package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

import java.util.Collections;
import java.util.Set;

@Getter
public class YongHengShouWangZSParam {

    /** 自己所需位置 */
    private Set<Integer> sequences = Collections.emptySet();
    /** BUFF标识 */
    private String buffId;
    /** BUFF添加的目标 */
    private String selectId;

}
