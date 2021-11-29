package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.service.buff.Buff;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.effect.Counter;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.OwnerDiePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.BuffUpdateParam;
import com.pangu.logic.module.battle.service.passive.param.PropagateCounterParam;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.skill.effect.BuffUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 带有感电状态的敌人被击杀时，会将层数传给附近的敌人
 */
@Component
public class PropagateCounter implements OwnerDiePassive {
    @Autowired
    private BuffUpdate buffUpdate;

    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attack, ITimedDamageReport timedDamageReport, int time, Context context) {
        //  能进此被动不代表目标已死亡，此被动优先级极低，持有者可能因为其他被动而复活
        if (!owner.isDead()) {
            return;
        }

        //  一堆没啥意义的健壮性校验
        final PropagateCounterParam param = passiveState.getParam(PropagateCounterParam.class);
        final BuffState buffState = owner.getBuffStateByTag(param.getBuffTag());
        if (buffState == null) {
            return;
        }
        final Buff buff = BuffFactory.getBuff(buffState.getType());
        final Counter counter = buff instanceof Counter ? ((Counter) buff) : null;
        if (counter == null) {
            return;
        }

        //  正式逻辑
        //  获取层数
        final Integer count = buffState.getAddition(Integer.class, 0);

        //  计算传染目标
        final List<Unit> select = TargetSelector.select(owner, param.getTarget(), time);
        //  构建更新参数
        final BuffUpdateParam buffUpdateParam = new BuffUpdateParam(buffState.getId(), 0, true, count);
        //  传染
        for (Unit unit : select) {
            buffUpdate.doBuffUpdate(buffUpdateParam, owner, unit, timedDamageReport, time);
        }
    }

    @Override
    public PassiveType getType() {
        return PassiveType.PROPAGATE_COUNTER;
    }
}
