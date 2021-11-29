package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.service.buff.Buff;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.buff.param.IntervalRangeAuraParam;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 连续处于指定范围超过X秒的目标，会添加BUFF
 * @author Kubby
 */
@Component
public class IntervalRangeAura implements Buff {

    @Override
    public BuffType getType() {
        return BuffType.INTERVAL_RANGE_AURA;
    }

    @Override
    public void update(BuffState state, Unit unit, int time, Object o) {
        List<Unit> targets = findTargets(state, unit, time);
        IntervalRangeAuraAddition addition = state
                .getAddition(IntervalRangeAuraAddition.class, new IntervalRangeAuraAddition());
        IntervalRangeAuraParam param = state.getParam(IntervalRangeAuraParam.class);
        List<Unit> addBuffUnits = addition.refresh(targets, param.getInterval(), time);

        for (Unit addBuffUnit : addBuffUnits) {
            BuffFactory.addBuff(param.getBuffId(), unit, addBuffUnit, time, state.getBuffReport(), null);
        }
    }

    private List<Unit> findTargets(BuffState state, Unit unit, int time) {
        IntervalRangeAuraParam param = state.getParam(IntervalRangeAuraParam.class);
        return TargetSelector.select(unit, param.getSelectId(), time);
    }

    public static class IntervalRangeAuraAddition {

        private Map<Unit, Integer> times = new HashMap<>();

        public List<Unit> refresh(List<Unit> units, int interval, int time) {
            removeInvalid(units);
            addNew(units, time);
            return takeResult(interval, time);
        }

        private void removeInvalid(List<Unit> units) {
            Iterator<Map.Entry<Unit, Integer>> iterator = times.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Unit, Integer> entry = iterator.next();
                if (!units.contains(entry.getKey())) {
                    iterator.remove();
                }
            }
        }

        private void addNew(List<Unit> units, int time) {
            for (Unit unit : units) {
                times.computeIfAbsent(unit, key -> time);
            }
        }

        private List<Unit> takeResult(int interval, int time) {
            List<Unit> result = new LinkedList<>();
            Iterator<Map.Entry<Unit, Integer>> iterator = times.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Unit, Integer> entry = iterator.next();
                if (entry.getValue() + interval >= time) {
                    result.add(entry.getKey());
                    iterator.remove();
                }
            }
            return result;
        }

        public void clear() {
            times.clear();
        }
    }
}
