package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.resource.BuffSetting;
import com.pangu.logic.module.battle.service.buff.Buff;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.buff.param.EnergyBarrierParam;
import com.pangu.logic.module.battle.service.core.Unit;
import org.springframework.stereotype.Component;

/**
 * 能量屏障
 * 制造一个圆形区域（罩子，半径1.5格），区域内自己攻速提升10%，受到伤害降低10%。屏障持续6秒
 */
@Component
public class EnergyBarrier implements Buff {
    @Override
    public BuffType getType() {
        return BuffType.ENERGY_BARRIER;
    }

    @Override
    public boolean add(BuffState state, Unit unit, int time) {
        Point point = unit.getPoint();
        EnergyBarrierValue va = new EnergyBarrierValue(point.x, point.y);
        state.setAddition(va);
        unit.addBuff(state);
        return true;
    }

    @Override
    public void update(BuffState state, Unit unit, int time, Object o) {
        EnergyBarrierParam param = state.getParam(EnergyBarrierParam.class);
        int distance = param.getDistance();
        EnergyBarrierValue addition = state.getAddition(EnergyBarrierValue.class);
        Point circlePoint = addition.position;
        int dis = circlePoint.distance(unit.getPoint());
        if (dis < distance) {
            return;
        }
        String buffId = param.getBuff();
        BuffFactory.addBuff(buffId, unit, unit, time, state.getBuffReport(), null);
    }

    @Override
    public void remove(BuffState state, Unit unit, int time) {
        unit.removeBuff(state);

        EnergyBarrierParam param = state.getParam(EnergyBarrierParam.class);
        String buffId = param.getBuff();
        BuffSetting setting = BuffFactory.getSetting(buffId);
        String tag = setting.getTag();
        BuffState buffState = unit.getBuffStateByTag(tag);
        if (buffState == null) {
            return;
        }
        BuffFactory.removeBuffState(buffState, unit, time);

    }

    private static class EnergyBarrierValue {
        Point position;

        public EnergyBarrierValue(int x, int y) {
            this.position = new Point(x, y);
        }
    }
}
