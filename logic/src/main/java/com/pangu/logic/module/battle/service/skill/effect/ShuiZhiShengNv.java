package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.FilterType;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.StateRemove;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.DispelType;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 随机驱散我方两个负面状态，包含harmful buff和harmful state
 */
@Component
public class ShuiZhiShengNv implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.SHUI_ZHI_SHENG_NV;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final Integer dispelCount = state.getParam(Integer.class);
        if (dispelCount < 1) {
            return;
        }

        final List<Unit> friends = FilterType.FRIEND.filter(owner, time);
        List<UnitHarmRef> unitHarmRefs = new ArrayList<>();
        for (Unit friend : friends) {
            final List<BuffState> deBuffs = friend.getBuffByDispel(DispelType.HARMFUL);
            final List<UnitState> harmStates = friend.getStates(true, time);

            //统计debuff和负面状态
            if (!CollectionUtils.isEmpty(deBuffs)) {
                unitHarmRefs.addAll(deBuffs.stream().map(deBuff -> new UnitHarmRef(friend, deBuff)).collect(Collectors.toList()));
            }
            if (!CollectionUtils.isEmpty(harmStates)) {
                unitHarmRefs.addAll(harmStates.stream().map(harmState -> new UnitHarmRef(friend, harmState)).collect(Collectors.toList()));
            }
        }

        if (unitHarmRefs.isEmpty()) {
            return;
        }

        Collections.shuffle(unitHarmRefs);
        final int count = Math.min(dispelCount, unitHarmRefs.size());
        for (int i = 0; i < count; i++) {
            UnitHarmRef.dispel(unitHarmRefs.get(i), time, skillReport);
        }
    }

    @AllArgsConstructor
    private static class UnitHarmRef {
        private Unit owner;
        private Object harm;

        static void dispel(UnitHarmRef unitHarmRef, int time, ITimedDamageReport timedDamageReport) {
            final Object harm = unitHarmRef.harm;
            final Unit owner = unitHarmRef.owner;

            if (harm instanceof BuffState) {
                BuffFactory.removeBuffState((BuffState) harm, owner, time);
            } else if (harm instanceof UnitState) {
                final UnitState state = (UnitState) harm;
                owner.removeState(state);
                timedDamageReport.add(time, owner.getId(), new StateRemove(Collections.singletonList(state)));
            }
        }
    }
}
