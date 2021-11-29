package com.pangu.logic.module.battle.service.select.filter;

import com.pangu.logic.module.battle.service.core.Unit;

import java.util.List;

/**
 * 忽略魅惑的干扰，强制选择正确的敌方
 */
public class ForceEnemy implements Filter {
    @Override
    public List<Unit> filter(Unit unit, int time) {
        return unit.getEnemy().getCurrent();
    }
}
