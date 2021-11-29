package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.BuffReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.service.buff.Buff;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import org.springframework.stereotype.Component;


/**
 *  定时炸弹型buff，当buff移除时，将addition中的伤害一次性结算给持有者
 */
@Component
public class Bomb implements Buff {
    @Override
    public BuffType getType() {
        return BuffType.BOMB;
    }

    @Override
    public void update(BuffState state, Unit unit, int time, Object addition) {

    }

    @Override
    public void remove(BuffState state, Unit unit, int time) {
        if (!unit.removeBuff(state)) {
            return;
        }
        final Long accDmg = state.getAddition(Long.class, 0L);
        if (accDmg >= 0) {
            return;
        }
        final Context context = new Context(unit);
        context.addValue(unit, AlterType.HP, accDmg);
        final BuffReport buffReport = state.getBuffReport();
        buffReport.add(time, unit.getId(), Hp.of(accDmg));
        context.execute(time, buffReport);
    }
}
