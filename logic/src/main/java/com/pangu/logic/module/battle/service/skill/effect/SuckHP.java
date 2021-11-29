package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import org.springframework.stereotype.Component;

/**
 * 伤害吸血
 */
@Component
public class SuckHP implements SkillEffect {

    @Override
    public EffectType getType() {
        return EffectType.SUCK;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        // 迭代处理值变更信息
        long damage = context.getHpChange(target);
        if (damage >= 0) {
            return;
        }
        double rate = state.getParam(Double.class);
        long value = (long) (-rate * damage);

        // 将值变更作用到上下文
        context.addValue(owner, AlterType.HP, value);
        skillReport.add(time, owner.getId(), Hp.of(value));
    }
}
