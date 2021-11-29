package com.pangu.logic.module.battle.service.skill.ctx;

import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class OwnerTargetCtx {
    private final int time;
    private final Unit owner;
    private final Unit target;
    private double factor;
    private Context context;

    public OwnerTargetCtx(int time, Unit owner, Unit target) {
        this.time = time;
        this.owner = owner;
        this.target = target;
    }

    public OwnerTargetCtx(int time, Unit owner, Unit target, double factor) {
        this.time = time;
        this.owner = owner;
        this.target = target;
        this.factor = factor;
    }
}
