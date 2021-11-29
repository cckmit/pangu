package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.service.buff.Buff;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 目标选择器，当自身未处于被嘲讽状态时，依照指定的目标选择配置追击目标
 */
@Component
public class TargetSelectorBuff implements Buff {
    @Override
    public BuffType getType() {
        return BuffType.TARGET_SELECTOR_BUFF;
    }

    @Override
    public boolean add(BuffState state, Unit unit, int time) {
        update(state, unit, time, null);
        return true;
    }

    @Override
    public void update(BuffState state, Unit unit, int time, Object addition) {
        if (unit.hasState(UnitState.SNEER, time)) {
            state.setAddition(true);
            return;
        }
        if (!state.getAddition(boolean.class, true) || unit.getTraceUnit() != null && unit.getTraceUnit().canSelect(time)) {
            return;
        }
        final String target = state.getParam(String.class);
        final List<Unit> units = TargetSelector.select(unit, target, time);
        if (units.isEmpty()) {
            return;
        }
        unit.addState(UnitState.ZHUI_JI, Integer.MAX_VALUE);
        unit.setTraceUnit(units.get(0));
        state.setAddition(false);
    }

    @Override
    public void remove(BuffState state, Unit unit, int time) {
        unit.removeState(UnitState.ZHUI_JI);
    }
}
