package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackBeforePassive;
import com.pangu.logic.module.battle.service.passive.PassiveFactory;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.InitSummonedParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 对召唤物进行额外的初始化处理
 */
public class InitSummoned implements AttackBeforePassive {
    @Override
    public void attackBefore(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time, Context context, SkillReport skillReport) {

    }

    @Override
    public void attackEnd(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time, Context context, SkillReport skillReport) {
        final InitSummonedParam param = passiveState.getParam(InitSummonedParam.class);
        if (!effectState.getId().equals(param.getEffectId())) {
            return;
        }
        final List<Unit> summoned = context.getSummonUnits();
        if (CollectionUtils.isEmpty(summoned)) {
            return;
        }
        final String buff = param.getBuff();
        final String passive = param.getPassive();
        final String[] inits = param.getInits();
        for (Unit unit : summoned) {
            BuffFactory.addBuff(buff, owner, unit, time, skillReport, null);

            if (StringUtils.isNotEmpty(passive)) {
                unit.addPassive(PassiveFactory.initState(passive, time), owner);
            }

            for (String init : inits) {
                unit.addInitEffect(init);
            }
        }
    }

    @Override
    public PassiveType getType() {
        return PassiveType.INIT_SUMMONED;
    }
}
