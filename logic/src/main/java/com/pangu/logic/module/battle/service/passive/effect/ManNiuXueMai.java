package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.AddPassive;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.model.report.values.StateRemove;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.DispelType;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveFactory;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.ManNiuXueMaiParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 蛮牛血脉
 * 自身血量低于25%时，移除所有异常状态，并获得回光效果2秒，期间受到的伤害会变成自身的生命回复，冷却75秒。
 * 2级：回光效果持续4秒
 * 3级：自身血量低于35%时触发该技能
 * 4级：冷却时间降低为60秒
 */
@Component
public class ManNiuXueMai implements DamagePassive {
    @Override
    public PassiveType getType() {
        return PassiveType.MAN_NIU_XUE_MAI;
    }

    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time, Context context, SkillState skillState, SkillReport skillReport) {
        long curHp = owner.getValue(UnitValue.HP);
        int rate = (int) ((curHp + damage) * 100 / owner.getValue(UnitValue.HP_MAX));
        ManNiuXueMaiParam param = passiveState.getParam(ManNiuXueMaiParam.class);
        if (rate > param.getActiveRate()) {
            return;
        }
        PassiveValue passiveValue = PassiveValue.of(passiveState.getId(), owner.getId());
        List<UnitState> removeStates = BuffFactory.dispelState(owner, DispelType.HARMFUL, time);
        if (removeStates.size() != 0) {
            passiveValue.add(new StateRemove(removeStates));
        }
        String passiveId = param.getPassiveId();
        PassiveState addPassive = PassiveFactory.initState(passiveId, time);
        owner.addPassive(addPassive, owner);

        passiveValue.add(new AddPassive(passiveId));

        skillReport.add(time, owner.getId(), passiveValue);

        passiveState.addCD(time);
    }
}
