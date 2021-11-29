package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.ReflectParam;
import com.pangu.framework.utils.math.RandomUtils;
import org.springframework.stereotype.Component;

/**
 * {@link PassiveType#REFLECT}类型的被动效果实现
 */
@Component
public class Reflect implements DamagePassive {

    @Override
    public PassiveType getType() {
        return PassiveType.REFLECT;
    }

    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (owner == attacker) {
            return;
        }

        if (!attacker.canSelect(time)) {
            return;
        }

        final long hpChange = context.getHpChange(owner);
        if (hpChange >= 0) {
            return;    // 没有产生伤害
        }

        ReflectParam param = passiveState.getParam(ReflectParam.class);
        // 检查效果能否触发
        double rate = param.getRate();
        if (rate > 0 && !RandomUtils.isHit(rate)) {
            return;    // 没法触发反射
        }
        passiveState.addCD(time);
        // 计算反射伤害量
        double factor = param.getFactor();
        long value = (long) (hpChange * factor);
        context.addPassiveValue(attacker, AlterType.HP, value);
        skillReport.add(time, attacker.getId(), PassiveValue.single(passiveState.getId(), owner.getId(), Hp.of(value)));
    }
}
