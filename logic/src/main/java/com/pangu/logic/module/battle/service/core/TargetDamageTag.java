package com.pangu.logic.module.battle.service.core;

import lombok.Data;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

/**
 * 目标在这次攻击变更的状态
 */
@Data
@ToString
public class TargetDamageTag {

    // 暴击
    private boolean crit;

    // 魔法伤害
    private boolean magic;

    // 未被命中
    private boolean miss;

    // 其他标记用TAG
    private Set<String> tags;

    public void addTag(String tag) {
        if (tags == null) {
            tags = new HashSet<>();
        }
        tags.add(tag);
    }

    public boolean hasTag(String tag) {
        return tags != null && tags.contains(tag);
    }
}
