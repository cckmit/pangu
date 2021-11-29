package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.report.BuffReport;
import com.pangu.logic.module.battle.model.report.values.Mark;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.buff.param.GanDianGuangHuanParam;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.utils.SkillUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class GanDianGuangHuan extends Counter {
    @Override
    public BuffType getType() {
        return BuffType.GAN_DIAN_GUANG_HUAN;
    }

    @Override
    public boolean add(BuffState state, Unit unit, int time) {
        final Object addition = state.getAddition();
        if (!(addition instanceof Integer))
            return false;
        unit.addBuff(state);
        final Integer addPerUpdate = (Integer) addition;
        state.setAddition(0);
        update(state, unit, time, addPerUpdate);
        return true;
    }

    @Override
    public void update(BuffState state, Unit unit, int time, Object addPerUpdate) {
        if (!(state.getAddition() instanceof Integer) || !(addPerUpdate instanceof Integer)) return;
        Integer accumulates = (Integer) state.getAddition();
        accumulates += (Integer) addPerUpdate;
        final GanDianGuangHuanParam param = state.getParam(GanDianGuangHuanParam.class);
        //未满足触发层数，不触发
        if (accumulates < param.getTriggerCount()) {
            state.setAddition(accumulates);
            state.getBuffReport().add(time, unit.getId(), new Mark(accumulates));
            return;
        }
        //触发成功后重置层数
        accumulates = 0;
        state.setAddition(accumulates);
        state.getBuffReport().add(time, unit.getId(), new Mark(accumulates));
        //执行触发效果
        final Map<UnitState, Integer> stateMap = param.getState();
        final Set<Map.Entry<UnitState, Integer>> entries = stateMap.entrySet();
        final Unit caster = state.getCaster();
        for (Map.Entry<UnitState, Integer> entry : entries) {
            final UnitState type = entry.getKey();
            final Integer valid = entry.getValue();
            if (type.immune != null && type.immune.length != 0) {
                for (UnitState immu : type.immune) {
                    if (unit.hasState(immu, time)) {
                        return;
                    }
                }
            }
            BuffReport buffReport = state.getBuffReport();
            Context context = new Context(caster);
            SkillUtils.addState(caster, unit, type, time, time + valid, buffReport, context);
            context.execute(time, buffReport);
        }
    }
}
