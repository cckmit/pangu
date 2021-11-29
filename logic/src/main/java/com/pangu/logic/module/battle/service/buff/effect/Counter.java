package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.model.report.values.Mark;
import com.pangu.logic.module.battle.service.buff.Buff;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.buff.param.CounterParam;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveFactory;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 通用计数器BUFF
 */
@Component
@Setter
public class Counter implements Buff {
    private Callback callback;

    @PostConstruct
    protected void init() {
        callback = new Callback() {
            @Override
            public void exeWhenCountMax(BuffState state, Unit unit, int time) {

            }
        };
    }

    @Override
    public BuffType getType() {
        return BuffType.COUNTER;
    }

    @Override
    public boolean add(BuffState state, Unit unit, int time) {
        unit.addBuff(state);
        Integer initCount = state.getAddition(Integer.class);
        if (initCount == null) {
            initCount = 1;
        }
        state.setAddition(0);
        update(state, unit, time, initCount);

        //添加被动
        final String passiveId = state.getParam(CounterParam.class).getPassiveId();
        if (StringUtils.isEmpty(passiveId)) {
            return true;
        }
        PassiveState passiveState = PassiveFactory.initState(passiveId, time);
        unit.addPassive(passiveState, state.getCaster());
        return true;
    }

    @Override
    public void update(BuffState state, Unit unit, int time, Object addition) {
        final CounterParam param = state.getParam(CounterParam.class);
        final int maxCount = param.getMaxCount();
        int addCount = 1;
        if (addition instanceof Integer) addCount = (Integer) addition;
        final Integer preCount = state.getAddition(Integer.class);
        int curCount = Math.max(preCount + addCount, 0);
        curCount = Math.min(curCount, maxCount);
        if (preCount != curCount) {
            state.setAddition(curCount);
            state.getBuffReport().add(time, unit.getId(), new Mark(curCount));
            callback.exePostCountChange(state, unit, time);
        }
        if (curCount == maxCount) {
            callback.exeWhenCountMax(state, unit, time);
        }
    }

    @Override
    public void remove(BuffState state, Unit unit, int time) {
        if (!unit.removeBuff(state)) {
            return;
        }
        final String passiveId = state.getParam(CounterParam.class).getPassiveId();
        if (StringUtils.isEmpty(passiveId)) {
            return;
        }
        PassiveState passiveState = unit.getPassiveStates(passiveId);
        if (passiveState == null) {
            return;
        }
        unit.removePassive(passiveState);
    }


    protected interface Callback {
        //  当计数发生变更时调用的勾子
        default void exePostCountChange(BuffState state, Unit unit, int time) {
        }

        //  当计数到达最大值时调用的勾子
        default void exeWhenCountMax(BuffState state, Unit unit, int time) {
        }
    }
}
