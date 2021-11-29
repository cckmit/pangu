package com.pangu.logic.module.battle.service.select.filter;

import com.pangu.logic.module.battle.service.core.Unit;

import java.util.List;
import java.util.stream.Collectors;

public class FriendWithoutSelfFilter implements Filter {
    @Override
    public List<Unit> filter(Unit unit, int time) {
        return unit.getFriend().getCurrent().stream()
                .filter(friend -> friend != unit).collect(Collectors.toList());
    }
}
