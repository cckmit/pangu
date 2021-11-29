package com.pangu.logic.module.battle.service.core;

import com.pangu.logic.module.battle.model.UnitState;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 英雄状态添加上下文
 */
@Getter
public class UnitStateContext {

    // 有害的状态
    private Map<UnitState, Integer> harmState;
    //  控制状态
    private Map<UnitState, Integer> controlState;

    // 普通状态
    private Map<UnitState, Integer> normalState;

    public void add(UnitState type, int time) {
        if (type.harm) {
            if (harmState == null) {
                harmState = new HashMap<>(4);
            }
            harmState.merge(type, time, Math::max);

            if (type.controlState()) {
                if (controlState == null) {
                    controlState = new HashMap<>(4);
                }
                controlState.merge(type, time, Math::max);
            }
        } else {
            if (normalState == null) {
                normalState = new HashMap<>(4);
            }
            normalState.merge(type, time, Math::max);
        }
    }

    public boolean hasHarm() {
        return harmState != null && harmState.size() != 0;
    }

    public boolean toBeControlled() {
        for (UnitState state : harmState.keySet()) {
            if (UnitState.CONTROL_STATE.contains(state)) {
                return true;
            }
        }
        return false;
    }

    public void clearHarm() {
        if (harmState == null) {
            return;
        }
        harmState.clear();
        harmState = null;
    }

    public void decontrol() {
        if (controlState == null) {
            return;
        }
        for (Map.Entry<UnitState, Integer> e : controlState.entrySet()) {
            final UnitState controlState = e.getKey();
            harmState.remove(controlState);
        }
        controlState.clear();
        controlState = null;
    }
}
