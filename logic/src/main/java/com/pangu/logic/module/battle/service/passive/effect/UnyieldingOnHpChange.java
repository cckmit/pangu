package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.model.report.values.StateAdd;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.UnitHpChangePassive;
import com.pangu.logic.module.battle.service.passive.param.UnyieldingOnHpChangeParam;
import org.springframework.stereotype.Component;

import java.util.Set;


/**
 * 受到致命伤害时，生成一个护盾，免疫4秒内的一切伤害，并且每秒回复5%最大生命值的血量 <br>
 * 对buff造成的伤害也生效
 */
@Component
public class UnyieldingOnHpChange implements UnitHpChangePassive {
    @Override
    public PassiveType getType() {
        return PassiveType.UNYIELDING_ON_HP_CHANGE;
    }

    @Override
    public void hpChange(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> changeUnit) {
        long ownerCurHp = owner.getValue(UnitValue.HP);
        if (ownerCurHp > 0  //未受到致命伤害
                || owner.isDead()  //被即死
                || owner.hasState(UnitState.WU_DI_INVALID, time) //存在[无敌失效]状态
        ) {
            return;
        }

        passiveState.addCD(time);

        /* 添加HOT*/
        final UnyieldingOnHpChangeParam param = passiveState.getParam(UnyieldingOnHpChangeParam.class);
        BuffFactory.addBuff(param.getBuffId(), owner, owner, time, damageReport, null);

        /* 确保直接修改生命能够进入afterValue*/
        owner.setValue(UnitValue.HP, 1);
        context.addPassiveValue(owner, AlterType.HP, 0);

        /* 添加一个无敌状态，此处不走状态添加上下文，因为上下文状态生效时间太晚*/
        final int duration = param.getDuration();
        int validTime;
        if (duration > 0) {
            validTime = time + duration;
        } else {
            validTime = passiveState.getTime();
        }
        owner.addState(UnitState.WU_DI, validTime);
        damageReport.add(time, owner.getId(), PassiveValue.single(passiveState.getId(), owner.getId(), new StateAdd(UnitState.WU_DI, validTime)));
    }
}
