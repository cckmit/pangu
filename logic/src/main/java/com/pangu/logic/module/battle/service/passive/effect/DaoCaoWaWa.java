package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.*;
import com.pangu.framework.utils.math.RandomUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 稻草娃娃 使随机一名敌人受到的伤害增加100%，持续15秒。该单位在持续时间内死亡时会将该效果随机传递给另一名敌人。
 */
@Component
public class DaoCaoWaWa implements DamagePassive, OwnerDiePassive {
    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final Double rate = passiveState.getParam(Double.class);
        final long increaseValue = (long) (rate * damage);
        context.addPassiveValue(owner, AlterType.HP, increaseValue);
        skillReport.add(time, owner.getId(), PassiveValue.single(passiveState.getId(), owner.getId(), Hp.of(increaseValue)));
        passiveState.addCD(time);
    }

    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attack, ITimedDamageReport timedDamageReport, int time, Context context) {
        final List<Unit> current = owner.getFriend().getCurrent();
        if (current.isEmpty()) {
            return;
        }
        final int i = RandomUtils.nextInt(current.size());
        final Unit unit = current.get(i);
        final PassiveState state = PassiveFactory.initState(passiveState.getId(), time);
        unit.addPassive(state, owner);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.DCWW;
    }
}
