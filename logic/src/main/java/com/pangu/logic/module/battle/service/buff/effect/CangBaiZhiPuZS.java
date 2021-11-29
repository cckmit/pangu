package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.BuffReport;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.buff.Buff;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 1：任何自身造成的暴击伤害会有20%概率给目标一层印记
 * 10：有30%概率给目标一层印记
 * 20：有50%概率给目标一层印记
 * 30：场上每存在一个印记则自身攻速+5%
 */
@Component
public class CangBaiZhiPuZS implements Buff {
    @Override
    public BuffType getType() {
        return BuffType.CANG_BAI_ZHI_PU_ZS;
    }

    @Override
    public boolean add(BuffState state, Unit unit, int time) {
        unit.addBuff(state);
        state.setAddition(0);
        return true;
    }


    @Override
    public void update(BuffState state, Unit unit, int time, Object addition) {
        final Double normalSpdUpPerMark = state.getParam(Double.class);
        final List<Unit> enemies = unit.getEnemy().getCurrent();
        int curTotalMarkCount = 0;
        for (Unit enemy : enemies) {
            final BuffState buffState = enemy.getBuffStateByTag("shen_pan_yin_ji");
            if (buffState != null) {
                curTotalMarkCount += buffState.getAddition(Integer.class);
            }
        }

        final Integer preTotalMarkCount = state.getAddition(Integer.class);
        final int totalMarkCountChange = curTotalMarkCount - preTotalMarkCount;
        if (totalMarkCountChange == 0) {
            return;
        }
        final double normalSpdUp = totalMarkCountChange * normalSpdUpPerMark;
        final Context context = new Context(state.getCaster());
        context.addValue(unit, AlterType.RATE_NORMAL_SKILL_UP, normalSpdUp);
        final BuffReport buffReport = state.getBuffReport();
        buffReport.add(time, unit.getId(), new UnitValues(AlterType.RATE_NORMAL_SKILL_UP, normalSpdUp));
        context.execute(time, buffReport);

        state.setAddition(curTotalMarkCount);
    }
}
