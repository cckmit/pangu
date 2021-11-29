package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.service.buff.Buff;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.core.Unit;
import org.springframework.stereotype.Component;

/**
 * 标记类BUFF，无任何效果
 * @author Kubby
 */
@Component
public class Flag implements Buff {

    @Override
    public BuffType getType() {
        return BuffType.FLAG;
    }

    @Override
    public void update(BuffState state, Unit unit, int time, Object o) {
    }
}
