package com.pangu.logic.module.battle.model.report.values;


import com.pangu.framework.protocol.annotation.Transable;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * buff添加战报
 */
@Getter
@Transable
public class BuffAdd implements IValues {
    @Getter(AccessLevel.PRIVATE)
    private ValuesType type = ValuesType.BUFF_ADD;

    private String buffId;

    private String caster;

    private int index;

    public static BuffAdd of(String buffId, String caster,int index) {
        BuffAdd buffAdd = new BuffAdd();
        buffAdd.buffId = buffId;
        buffAdd.caster = caster;
        buffAdd.index = index;
        return buffAdd;
    }
}
