package com.pangu.logic.module.battle.service.skill.ctx;

import com.pangu.logic.module.battle.service.core.Unit;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class HpRecoverCtx {
    private final Unit owner;
    private final Unit target;

    // 伤害比率
    private final double factor;

    private final double percent;

    private final double rate;

}
