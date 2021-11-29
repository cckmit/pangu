package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public class AddBuffWhenDamageParam {

    /** 添加的BUFF标识 */
    private List<String> buffs = Collections.emptyList();

    /** 触发概率 */
    private double prob = 1;

    /** 添加目标*/
    private String targetId;

    public static String MURDERER = "MURDERER";
}
