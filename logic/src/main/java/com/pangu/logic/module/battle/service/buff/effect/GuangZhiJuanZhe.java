package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.FilterType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.service.buff.Buff;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.buff.param.GuangZhiJuanZheParam;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import org.springframework.stereotype.Component;


/**
 * 光之眷者
 * 战斗中,存活的友军英雄越多,每名存活的友军使自己活得2%的伤害减免
 * 2级:免伤提升至4%
 * 3级:每名存活的友军使自己获得2%的攻击提升
 * 4级:每死亡一名友军英雄时获得200能量
 */
@Component
public class GuangZhiJuanZhe implements Buff {
    @Override
    public BuffType getType() {
        return BuffType.GUANG_ZHI_JUAN_ZHE;
    }

    @Override
    public boolean add(BuffState state, Unit unit, int time) {
        unit.addBuff(state);
        //统计存活友军的数量
        final int count = FilterType.FRIEND.filter(unit, time).size() - 1;
        final GuangZhiJuanZheParam param = state.getParam(GuangZhiJuanZheParam.class);

        //根据存活友军数量为自身提供免伤
        final Context context = new Context(unit);
        final double dmgCutRate = param.getDmgCutRatePerLivingFriend() * count;
        context.addPassiveValue(unit, AlterType.RATE_UNHARM_M, dmgCutRate);
        context.addPassiveValue(unit, AlterType.RATE_UNHARM_P, dmgCutRate);

        //根据存活友军数量为自身提供攻击
        final double dmgUpRate = param.getAtkUpRatePerLivingFriend() * count;
        context.addPassiveValue(unit, AlterType.ATTACK_M, unit.getOriginValue(UnitValue.ATTACK_M) * dmgUpRate);
        context.addPassiveValue(unit, AlterType.ATTACK_P, unit.getOriginValue(UnitValue.ATTACK_P) * dmgUpRate);
        context.execute(time, state.getBuffReport());

        //缓存当前存活友军数量
        state.setAddition(count);

        return true;
    }

    @Override
    public void update(BuffState state, Unit unit, int time, Object addition) {
        final int curCount = FilterType.FRIEND.filter(unit, time).size();
        final Integer originCount = state.getAddition(Integer.class);
        //监测存活单位变化量
        final int delta = curCount - originCount;
        //无变化不做任何处理
        if (delta == 0) {
            return;
        }
        //根据变化量修改当前属性
        final Context context = new Context(unit);
        final GuangZhiJuanZheParam param = state.getParam(GuangZhiJuanZheParam.class);
        final double dmgCutRate = param.getDmgCutRatePerLivingFriend() * delta;
        context.addPassiveValue(unit, AlterType.RATE_UNHARM_M, dmgCutRate);
        context.addPassiveValue(unit, AlterType.RATE_UNHARM_P, dmgCutRate);
        final double dmgUpRate = param.getAtkUpRatePerLivingFriend() * delta;
        context.addPassiveValue(unit, AlterType.ATTACK_M, unit.getOriginValue(UnitValue.ATTACK_M) * dmgUpRate);
        context.addPassiveValue(unit, AlterType.ATTACK_P, unit.getOriginValue(UnitValue.ATTACK_P) * dmgUpRate);
        context.execute(time, state.getBuffReport());

        //缓存当前值
        state.setAddition(curCount);
    }
}
