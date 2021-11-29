package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Getter
public class ShenYuanTuFuZSParam {

    /** 无情钩链技能的标签 */
    private Set<String> skillTags = Collections.emptySet();
    /** 释放无情钩链时加的BUFF标识 */
    private List<String> buffIds = Collections.emptyList();
    /** 大招伤害提升所需低于的血量比率 */
    private double harmUpHpPct;
    /** 大招伤害提升率 */
    private double harmUpRate;
    /** 大招伤害提升CD */
    private double harmUpCd;

}
