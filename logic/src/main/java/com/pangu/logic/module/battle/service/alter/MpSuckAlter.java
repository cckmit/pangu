package com.pangu.logic.module.battle.service.alter;

import com.pangu.logic.module.battle.service.core.Unit;

/**
 * Mp吸收修改器，不受mp_add_rate影响
 */
public class MpSuckAlter extends MpAlter{
    @Override
    protected long cal(Unit unit, long value) {
        return value;
    }
}
