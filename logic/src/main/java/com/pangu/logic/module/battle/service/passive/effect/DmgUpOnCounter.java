package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.effect.Counter;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.DmgUpOnCounterParam;
import org.springframework.stereotype.Component;

/**
 * 特尤斯攻击带有感电状态的目标时伤害会额外提升，每一层提供15%伤害提升
 */
@Component
public class DmgUpOnCounter implements AttackPassive {
    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final DmgUpOnCounterParam param = passiveState.getParam(DmgUpOnCounterParam.class);
        final BuffState buffState = target.getBuffStateByTag(param.getBuffTag());
        if (buffState == null) {
            return;
        }
        if (!(BuffFactory.getBuff(buffState.getType()) instanceof Counter)) {
            return;
        }
        final Integer count = buffState.getAddition(Integer.class, 0);
        final long dmgChange = (long) (count * param.getRatePerCount() * damage);
        context.addPassiveValue(target, AlterType.HP, dmgChange);
        skillReport.add(time, target.getId(), PassiveValue.single(passiveState.getId(), owner.getId(), Hp.of(dmgChange)));
    }

    @Override
    public PassiveType getType() {
        return PassiveType.DMG_UP_ON_COUNTER;
    }
}
