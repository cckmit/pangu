package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import com.pangu.logic.module.battle.service.skill.param.WuGuGuiShuParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class WuGuGuiShu implements SkillEffect {

    @Autowired
    private HpMagicDamage magicDamage;

    @Override
    public EffectType getType() {
        return EffectType.WU_GU_GUI_SHU;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time,
                        SkillState skillState, Context context) {
        WuGuGuiShuAddition addition = state.getAddition(WuGuGuiShuAddition.class, WuGuGuiShuAddition.of(time));
        /* 重置伤害目标 */
        if (addition.time != time) {
            addition = WuGuGuiShuAddition.of(time);
            state.setAddition(addition);
        }

        WuGuGuiShuParam param = state.getParam(WuGuGuiShuParam.class);

        List<Unit> rounds = TargetSelector.select(target, param.getTargetRoundSelectId(), time);
        rounds.remove(target);
        if (rounds.isEmpty()) {
            return;
        }

        EffectState damageEffectState = new EffectState(null, 0);
        damageEffectState.setParamOverride(new DamageParam(param.getFactor()));
        for (Unit enemy : rounds) {
            if (addition.hurts.contains(enemy)) {
                continue;
            }
            magicDamage.execute(damageEffectState, owner, enemy, skillReport, time, null, context);
            addition.hurts.add(enemy);
        }
    }

    private static class WuGuGuiShuAddition {

        private int time;
        private Set<Unit> hurts = new HashSet<>();

        public static WuGuGuiShuAddition of(int time) {
            WuGuGuiShuAddition addition = new WuGuGuiShuAddition();
            addition.time = time;
            return addition;
        }

    }
}
