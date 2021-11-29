package com.pangu.logic.module.battle.model.report.values;

import com.pangu.framework.protocol.annotation.Transable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 * 变身集合
 * */
@Transable
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TransformValue implements IValues {
    @Getter(AccessLevel.PRIVATE)
    private ValuesType type = ValuesType.TRANSFORM;
    //1:变身 0:变回
    int state;

    public TransformValue(int state) {
        this.state = state;
    }

}
