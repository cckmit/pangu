package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Death;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.KillParam;
import org.springframework.stereotype.Component;

/**
 * 即死效果
 */
@Component
public class Kill implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.KILL;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final KillParam param = state.getParam(KillParam.class);

        //判断目标是否可免疫此次死亡
        for (UnitState immuneState : param.getImmuneStates()) {
            if (target.hasState(immuneState, time)) {
                return;
            }
        }

        //设置角色死亡状态
        if (param.isRevivable()) {
            target.dead();
        } else {
            target.foreverDead();
        }

        //造成当前生命和护盾之和的伤害，以便触发目标死亡被动
        final long hpChange = -target.getValue(UnitValue.HP) - target.getValue(UnitValue.SHIELD);
        context.addValue(target, AlterType.HP, hpChange);
        skillReport.add(time, target.getId(), Hp.of(hpChange));
        skillReport.add(time, target.getId(), new Death());
    }
}
