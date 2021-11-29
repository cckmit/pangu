package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import org.springframework.stereotype.Component;

/**
 * 交换目标生命值
 */
@Component
public class HpExchange implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.HP_EXCHANGE;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final long ownerHpChange = target.getValue(UnitValue.HP) - owner.getValue(UnitValue.HP);

        context.addValue(owner, AlterType.HP, ownerHpChange);
        context.addValue(target, AlterType.HP, -ownerHpChange);

        skillReport.add(time, owner.getId(), Hp.of(ownerHpChange));
        skillReport.add(time, target.getId(), Hp.of(-ownerHpChange));
    }
}
