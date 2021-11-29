package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.service.buff.Buff;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.buff.param.SelfCircleCheckParam;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveFactory;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 使周围敌方英雄的防御降低10%
 * 用于周期性判断是否有英雄进入自己周围
 * 无双光环：
 * 使周围敌方英雄的防御降低10%。光环影响下的敌方英雄阵亡后，无双战姬将获得阵亡武将的10%攻击力加成，持续5秒
 * 2级：获得阵亡武将的15%攻击力加成
 * 3级：使周围敌方英雄的防御降低15%
 * 4级：敌方英雄阵亡时回复自身10%最大生命值的血量
 */
@Component
public class SelfCircleCheck implements Buff {
    @Override
    public BuffType getType() {
        return BuffType.SELF_CIRCLE_CHECK;
    }

    @Override
    public boolean add(BuffState state, Unit unit, int time) {
        unit.addBuff(state);
        SelfCircleCheckParam param = state.getParam(SelfCircleCheckParam.class);
        String passiveId = param.getPassiveId();
        if (StringUtils.isNotEmpty(passiveId)) {
            PassiveState passiveState = PassiveFactory.initState(passiveId, time);
            unit.addPassive(passiveState, state.getCaster());
        }
        // 立即生效一次
        update(state, unit, time, null);
        return true;
    }

    @Override
    public void update(BuffState state, Unit unit, int time, Object o) {
        SelfCircleCheckParam param = state.getParam(SelfCircleCheckParam.class);
        List<Unit> units;
        if (param.isFriend()) {
            units = unit.getFriend().getCurrent();
        } else {
            units = unit.getEnemy().getCurrent();
        }
        Set<Unit> inCircleUnits = new HashSet<>(units.size());
        Point point = unit.getPoint();
        int radius = param.getRadius();
        for (Unit item : units) {
            if (item.hasState(UnitState.UNVISUAL, time)) {
                continue;
            }
            int distance = item.getPoint().distance(point);
            if (distance <= radius) {
                inCircleUnits.add(item);
            }
        }
        if (!inCircleUnits.isEmpty()) {
            final String buff = param.getBuff();
            final Unit caster = state.getCaster();
            for (Unit needAdd : inCircleUnits) {
                BuffFactory.addBuff(buff, caster, needAdd, time, state.getBuffReport(), null);
            }
        }
    }

}
