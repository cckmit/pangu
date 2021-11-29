package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.service.buff.Buff;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import org.springframework.stereotype.Component;

@Component
public class DuYeZhiZhua implements Buff {
    @Override
    public BuffType getType() {
        return BuffType.DYZZ;
    }

    @Override
    public boolean add(BuffState state, Unit unit, int time) {
        unit.addBuff(state);
        final Double param = state.getParam(Double.class);
        final long max = Math.max(state.getCaster().getValue(UnitValue.ATTACK_P), state.getCaster().getValue(UnitValue.ATTACK_M));
        final Long damage = (long) (max * param);
        state.setAddition(damage);
        return true;
    }

    @Override
    public void update(BuffState state, Unit unit, int time, Object o) {
        final Long damage = state.getAddition(Long.class);
        if (damage == null) {
            return;
        }
        Context context = new Context(unit);
        context.addValue(unit, AlterType.HP, damage);
        state.getBuffReport().add(time, unit.getId(), Hp.of(damage));
    }
}
