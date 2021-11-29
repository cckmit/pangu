package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.action.SkillEffectAction;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.TwinsMeleeSpikeParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 对前方敌人造成多段伤害，每段造成60%伤害，最后一击会将星辰之力释放刺破星辰，对一条直线上的敌人造成150%穿透伤害
 * 2级：每段伤害增加至70%
 * 3级：最后一段伤害增加至180%
 * 4级：最后一段伤害增加至210%
 */
@Component
public class TwinsMeleeSpike implements SkillEffect {
    @Autowired
    private HpMagicDamage magicDamage;

    @Override
    public EffectType getType() {
        return EffectType.TWINS_MELEE_SPIKE;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final TwinsMeleeSpikeParam param = state.getParam(TwinsMeleeSpikeParam.class);

        final SkillEffectAction sea = context.getRootSkillEffectAction();
        final int loopTimes = context.getLoopTimes();

        if (loopTimes == sea.getTotalExecTimes()) {
            final List<Unit> select = TargetSelector.select(owner, param.getLastTarget(), time);
            state.setParamOverride(param.getLastDmgParam());
            for (Unit unit : select) {
                magicDamage.execute(state, owner, unit, skillReport, time, skillState, context);
            }
        } else {
            final List<Unit> select = TargetSelector.select(owner, param.getTarget(), time);
            state.setParamOverride(param.getDmgParam());
            for (Unit unit : select) {
                magicDamage.execute(state, owner, unit, skillReport, time, skillState, context);
            }
        }
        state.setParamOverride(null);
    }
}
