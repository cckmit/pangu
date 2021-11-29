package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.BuffReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.service.buff.Buff;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.buff.param.PowerParam;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import org.springframework.stereotype.Component;

/**
 * 月之石 能量大于50%时 血量持续回复 当有日之石时这个回复变为2倍
 */
@Component
public class MoonPower implements Buff {
    @Override
    public BuffType getType() {
        return BuffType.MOON_POWER;
    }

    @Override
    public void update(BuffState state, Unit unit, int time, Object o) {
        final PowerParam param = state.getParam(PowerParam.class);
        final double hpRate = param.getRate();
        final double rate = unit.getValue(UnitValue.MP) / 1D / unit.getValue(UnitValue.MP_MAX);
        if (rate < hpRate) {
            return;
        }

        final String tag = param.getTag();
        Context context = new Context(unit);
        final BuffReport buffReport = state.getBuffReport();
        long increaseHp;
        if (unit.getBuffStateByTag(tag) != null) {
            increaseHp = (long) (param.getTagRate() * unit.getValue(UnitValue.HP_MAX));
        } else {
            increaseHp = (long) (param.getIncreaseRate() * unit.getValue(UnitValue.HP_MAX));
        }
        context.addValue(unit, AlterType.HP, increaseHp);
        buffReport.add(time, unit.getId(), Hp.of(increaseHp));
        context.execute(time, buffReport);
    }

}
