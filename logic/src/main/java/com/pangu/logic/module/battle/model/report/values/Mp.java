package com.pangu.logic.module.battle.model.report.values;

import com.pangu.logic.module.battle.model.report.MpFrom;
import com.pangu.framework.protocol.annotation.Transable;
import lombok.*;

@Transable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Mp implements IValues {
    @Getter(AccessLevel.PRIVATE)
    private ValuesType type = ValuesType.MP;

    /**
     * 变更值
     */
    private long mp;

    /**
     * 变更来源
     */
    private MpFrom from;

    public Mp(long mp) {
        this.mp = mp;
        this.from = MpFrom.NORMAL;
    }

    public Mp(long mp, MpFrom from) {
        this.mp = mp;
        this.from = from;
    }
}
