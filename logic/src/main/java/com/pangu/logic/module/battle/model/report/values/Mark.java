package com.pangu.logic.module.battle.model.report.values;

import com.pangu.framework.protocol.annotation.Transable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 特殊印记战报
 */
@Transable
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Mark implements IValues{
    @Getter(AccessLevel.PRIVATE)
    private ValuesType type = ValuesType.MARK;


    /**
     * 印记当前数量
     */
    private int count;
    public Mark(int count){
        this.count = count;
    }
}
