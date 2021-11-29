package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.model.UnitRate;
import com.pangu.logic.module.battle.model.report.BuffReport;
import com.pangu.logic.module.battle.service.buff.Buff;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.buff.param.SuiYuanBiZhuParam;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.param.StateAddParam;
import com.pangu.logic.module.battle.service.skill.utils.SkillUtils;
import org.springframework.stereotype.Component;


/**
 * 虽远必诛
 * 自身周围的敌人，攻速每被降低10%，眩晕3秒，自身远处的敌人，攻速每被降低10%，造成的伤害减少5%
 */
@Component
public class SuiYuanBiZhu implements Buff {


    @Override
    public BuffType getType() {
        return BuffType.SUI_YUAN_BI_ZHU;
    }

    @Override
    public boolean add(BuffState state, Unit unit, int time) {
        unit.addBuff(state);
        update(state, unit, time, null);
        return true;
    }

    @Override
    public void update(BuffState state, Unit unit, int time, Object addition) {
        final SuiYuanBiZhuParam param = state.getParam(SuiYuanBiZhuParam.class);
        final Addition stateAddition = getAddition(state);
        final double preNormalAtkSpdRate = stateAddition.preNormalAtkSpdDown;
        final double curNormalAtkSpdRate = unit.getRate(UnitRate.NORMAL_SKILL_DOWN);
        final double rateChange = curNormalAtkSpdRate - preNormalAtkSpdRate;
        //仅累计普攻降低率增加时的变化值
        if (rateChange > 0) {
            stateAddition.accumNormalAtkSpdDown += rateChange;
        }
        //当累计值到达阈值时，触发特殊行为。并清空累计值
        if (stateAddition.accumNormalAtkSpdDown >= param.getTriggerAccumNormalAtkChangeRate()) {
            stateAddition.accumNormalAtkSpdDown = 0;
            final Unit caster = state.getCaster();
            final BuffReport buffReport = state.getBuffReport();
            if (unit.getPoint().distance(caster.getPoint()) < param.getBorder()) {
                //距离buff施法者较近时，添加指定异常
                final StateAddParam stateAddParam = param.getStateAddParam();
                final Context context = new Context(caster);
                SkillUtils.addState(caster, unit, stateAddParam.getState(), time, time + stateAddParam.getTime(), buffReport, context);
                context.execute(time, buffReport);
            } else {
                //距离buff施法者较远时，添加指定buff
                BuffFactory.addBuff(param.getBuffId(), caster, unit, time, buffReport, null);
            }
        }
        //记录本次观测值，供下次监测时计算变化值
        stateAddition.preNormalAtkSpdDown = curNormalAtkSpdRate;
    }

    private Addition getAddition(BuffState buffState) {
        Addition addition = buffState.getAddition(Addition.class);
        if (addition == null) {
            addition = new Addition();
            buffState.setAddition(addition);
        }
        return addition;
    }

    private static class Addition {
        //攻速降低率的累计值
        private double accumNormalAtkSpdDown;
        //上次监测时的攻速降低率
        private double preNormalAtkSpdDown;
    }
}
