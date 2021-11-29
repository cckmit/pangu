package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.service.buff.Buff;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.buff.param.CheckMpAddBuffsParam;
import com.pangu.logic.module.battle.service.core.Unit;
import org.springframework.stereotype.Component;

/**
 * 每当MP大于800时添加一个BUFF给作用者
 */
@Component
public class CheckMpAddBuffs implements Buff {
    @Override
    public BuffType getType() {
        return BuffType.CHECK_MP_ADD_BUFFS;
    }

    @Override
    public void update(BuffState state, Unit unit, int time, Object o) {
        final CheckMpAddBuffsParam param = state.getParam(CheckMpAddBuffsParam.class);
        final long value = unit.getValue(UnitValue.MP);
        if (value < param.getMp()) {
            return;
        }
        for (String buff : param.getBuffs()) {
            BuffFactory.addBuff(buff, state.getCaster(), unit, time, state.getBuffReport(), null);
        }

    }
}
