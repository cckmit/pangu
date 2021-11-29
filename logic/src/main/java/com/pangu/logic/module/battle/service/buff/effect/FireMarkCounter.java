package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.service.buff.Buff;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.buff.param.FireMarkCounterParam;
import com.pangu.logic.module.battle.service.core.Unit;
import org.springframework.stereotype.Component;

/**
 * 哪吒火焰计数器，该计数器计数改变会同步更新哪吒专属BUFF，使用这种方式同步数据比起无脑轮询具有更好的性能和准确度
 *
 * @see NeZhaZS
 */
@Component
public class FireMarkCounter extends Counter {

    @Override
    protected void init() {
        super.setCallback(new Callback() {
            @Override
            public void exePostCountChange(BuffState state, Unit unit, int time) {
                final FireMarkCounterParam param = state.getParam(FireMarkCounterParam.class);
                final Unit caster = state.getCaster();
                final BuffState buffStateByTag = caster.getBuffStateByTag(param.getBuffTag());
                if (buffStateByTag == null) {
                    return;
                }

                final Buff buff = BuffFactory.getBuff(buffStateByTag.getType());
                buff.update(buffStateByTag, caster, time, null);
            }
        });
    }

    @Override
    public BuffType getType() {
        return BuffType.FIRE_MARK_COUNTER;
    }
}
