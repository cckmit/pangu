package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.resource.FightSkillSetting;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackBeforePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.DuanBingParam;
import com.pangu.framework.resource.Storage;
import com.pangu.framework.resource.anno.Static;
import com.pangu.framework.utils.math.RandomUtils;
import org.springframework.stereotype.Component;

/**
 * 使用大招进攻时有概率无视目标防御
 */
@Component
public class DuanBing implements AttackBeforePassive {
    @Static
    private Storage<String, FightSkillSetting> skillStorage;

    private static class Addition {
        private long defMChange;
        private long defPChange;
    }

    @Override
    public void attackBefore(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time, Context context, SkillReport skillReport) {
        if (owner.isFriend(target)) {
            return;
        }
        final FightSkillSetting fightSkillSetting = skillStorage.get(skillReport.getSkillId(), false);
        if (fightSkillSetting == null || fightSkillSetting.getType() != SkillType.SPACE) {
            return;
        }

        final DuanBingParam param = passiveState.getParam(DuanBingParam.class);
        if (!RandomUtils.isHit(param.getTriggerRate())) {
            return;
        }

        final long defMChange = (long) (param.getDefenceIgnoreRate() * target.getOriginValue(UnitValue.DEFENCE_M));
        final long defPChange = (long) (param.getDefenceIgnoreRate() * target.getOriginValue(UnitValue.DEFENCE_P));
        target.increaseValue(UnitValue.DEFENCE_M, -defMChange);
        target.increaseValue(UnitValue.DEFENCE_P, -defPChange);

        final Addition addition = passiveState.getAddition(Addition.class, new Addition());
        addition.defMChange = defMChange;
        addition.defPChange = defPChange;
    }

    @Override
    public void attackEnd(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time, Context context, SkillReport skillReport) {
        if (owner.isFriend(target)) {
            return;
        }
        final Addition addition = passiveState.getAddition(Addition.class, new Addition());
        if (addition.defMChange == 0 && addition.defPChange == 0) {
            return;
        }

        target.increaseValue(UnitValue.DEFENCE_M, addition.defMChange);
        target.increaseValue(UnitValue.DEFENCE_P, addition.defPChange);
        addition.defPChange = 0;
        addition.defMChange = 0;
    }

    @Override
    public PassiveType getType() {
        return PassiveType.DUAN_BING;
    }
}
