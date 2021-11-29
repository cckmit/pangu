package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.BuffReport;
import com.pangu.logic.module.battle.service.buff.Buff;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.buff.param.YiWangShaLouParam;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import org.springframework.stereotype.Component;

/**
 * 遗忘沙漏
 */
@Component
public class YiWangShaLou implements Buff {
    @Override
    public BuffType getType() {
        return BuffType.YWSL;
    }

    @Override
    public boolean add(BuffState state, Unit unit, int time) {
        unit.addBuff(state);
        final YiWangShaLouParam param = state.getParam(YiWangShaLouParam.class);
        int mp = param.getMp();
        final double decreasePreFriend = param.getDecreasePreFriend();
        int number = 0;
        for (Unit friend : unit.getFriend().getCurrent()) {
            if (friend.getBuffStateByTag(state.getTag()) != null) {
                number++;
            }
        }
        mp = (int) (mp * (1 + decreasePreFriend * number));
        state.setAddition(mp);
        return true;
    }

    @Override
    public void update(BuffState state, Unit unit, int time, Object o) {
        final Integer addition = state.getAddition(Integer.class);
        if (addition == null) {
            return;
        }
        BuffReport buffReport = state.getBuffReport();
        Context context = new Context(state.getCaster());
        context.addValue(unit, AlterType.MP, addition);
        context.execute(time, buffReport);
    }
}
