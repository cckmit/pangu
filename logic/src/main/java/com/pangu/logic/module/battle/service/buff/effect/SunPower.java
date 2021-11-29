package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.BuffReport;
import com.pangu.logic.module.battle.model.report.values.Mp;
import com.pangu.logic.module.battle.service.buff.Buff;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.buff.param.PowerParam;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import org.springframework.stereotype.Component;

/**
 * 日之石 血量大于50%时 能量持续回复 当有月之石时这个回复变为2倍
 */
@Component
public class SunPower implements Buff {
    @Override
    public BuffType getType() {
        return BuffType.SUN_POWER;
    }

    @Override
    public void update(BuffState state, Unit unit, int time, Object o) {
        final PowerParam param = state.getParam(PowerParam.class);
        final double hpRate = param.getRate();
        final double rate = unit.getValue(UnitValue.HP) / 1D / unit.getValue(UnitValue.HP_MAX);
        if (rate < hpRate) {
            return;
        }

        final String tag = param.getTag();
        Context context = new Context(unit);
        final BuffReport buffReport = state.getBuffReport();
        long increaseMp;
        if (unit.getBuffStateByTag(tag) != null) {
            increaseMp = (long) (param.getTagRate() * unit.getValue(UnitValue.MP_MAX));
        } else {
            increaseMp = (long) (param.getIncreaseRate() * unit.getValue(UnitValue.MP_MAX));
        }
        context.addValue(unit, AlterType.MP, increaseMp);
        buffReport.add(time, unit.getId(), new Mp(increaseMp));
        context.execute(time, buffReport);
    }

}
