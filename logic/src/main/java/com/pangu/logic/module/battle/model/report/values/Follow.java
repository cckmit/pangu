package com.pangu.logic.module.battle.model.report.values;

import com.pangu.framework.protocol.annotation.Transable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 跟随变更战报
 */
@Getter
@Transable
@AllArgsConstructor
@NoArgsConstructor
public class Follow implements IValues {

    @Getter(AccessLevel.PRIVATE)
    private ValuesType type = ValuesType.FOLLOW;

    // 如鸟嘴医生id
    private String owner;

    // 被跟随者id
    private String target;

    public static Follow of(String owner, String target) {
        Follow follow = new Follow();
        follow.owner = owner;
        follow.target = target;
        return follow;
    }
}
