package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.BuffReport;
import com.pangu.logic.module.battle.model.report.values.Death;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import org.springframework.stereotype.Component;


/**
 * 死亡计数器，沿用父类的流程，在计数到达指定的最大值时，使计数器持有者死亡
 */
@Component
public class DeathCounter extends Counter {
    @Override
    protected void init() {
        super.setCallback(new Callback() {
            @Override
            public void exeWhenCountMax(BuffState state, Unit unit, int time) {
                final Context context = new Context(state.getCaster());
                final BuffReport buffReport = state.getBuffReport();
                unit.foreverDead();
                final long hpChange = -unit.getValue(UnitValue.HP) - unit.getValue(UnitValue.SHIELD);
                context.addValue(unit, AlterType.HP, hpChange);
                buffReport.add(time, unit.getId(), new Death());
                buffReport.add(time, unit.getId(), Hp.of(hpChange));
                context.execute(time, buffReport);
            }
        });
    }

    @Override
    public BuffType getType() {
        return BuffType.DEATH_COUNTER;
    }
}
