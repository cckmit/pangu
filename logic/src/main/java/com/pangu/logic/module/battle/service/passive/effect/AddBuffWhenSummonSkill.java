package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackBeforePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.AddBuffWhenSummonSkillParam;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 技能召唤时给召唤物添加BUFF
 *
 * @author Kubby
 */
@Component
public class AddBuffWhenSummonSkill implements AttackBeforePassive {

    @Override
    public void attackBefore(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time,
                             Context context, SkillReport skillReport) {

    }

    @Override
    public void attackEnd(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time,
                          Context context, SkillReport skillReport) {
        AddBuffWhenSummonSkillParam param = passiveState.getParam(AddBuffWhenSummonSkillParam.class);

        if (!param.getEffectTypes().contains(effectState.getType())) {
            return;
        }

        if (context.getSummonUnits() == null || context.getSummonUnits().isEmpty()) {
            return;
        }

        String[] buffs = param.getBuffs();
        List<Unit> summonList = new ArrayList<>(context.getSummonUnits());

        int overlayLimit = param.getOverlayLimit();
        Map<Unit, Integer> overlayTimes = passiveState.getAddition(Map.class, new HashMap());

        for (Unit summon : summonList) {
            int curr = overlayTimes.getOrDefault(summon, 0);
            if (curr >= overlayLimit) {
                continue;
            }
            Arrays.stream(buffs).forEach(buff -> BuffFactory.addBuff(buff, owner, summon, time, skillReport, null));
            overlayTimes.merge(summon, 1, Integer::sum);
        }

        passiveState.addCD(time);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.ADD_BUFF_WHEN_SUMMON_SKILL;
    }

}
