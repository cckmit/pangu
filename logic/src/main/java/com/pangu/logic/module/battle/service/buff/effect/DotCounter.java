package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.buff.param.DotCounterParam;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.effect.HpHigherDamage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 一个可叠加的流血debuff
 */
@Component
public class DotCounter extends Counter {
    @Autowired
    private HpHigherDamage higherDamage;

    @Override
    public BuffType getType() {
        return BuffType.DOT_COUNTER;
    }

    @Override
    public void update(BuffState state, Unit unit, int time, Object addition) {
        if (addition == null) {
            //  正常循环更新
            final DotCounterParam param = state.getParam(DotCounterParam.class);
            final HpHigherDamage.HigherDmgResult res = higherDamage.calcHigherDmgRes(state.getCaster(), unit, null, param.getDmgParam(), time);
            Context context = new Context(state.getCaster());
            //  根据层数计算总伤害
            context.addValue(unit, AlterType.HP, res.getDamage() * state.getAddition(Integer.class, 1));
            context.execute(time, state.getBuffReport());
        } else {
            //  叠加层数
            super.update(state, unit, time, addition);
        }
    }
}
