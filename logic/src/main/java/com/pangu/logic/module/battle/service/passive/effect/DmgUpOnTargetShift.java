package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.DmgUpOnTargetShiftParam;
import org.springframework.stereotype.Component;

/**
 * 在每攻击一个新目标时，康斯坦丁的普通攻击会造成额外的100%的物理伤害。
 */
@Component
public class DmgUpOnTargetShift implements AttackPassive {
    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final DmgUpOnTargetShiftParam param = passiveState.getParam(DmgUpOnTargetShiftParam.class);
        if (!skillState.getTag().equals(param.getTriggerTag())) {
            return;
        }
        final Unit preVictim = passiveState.getAddition(Unit.class);
        if (target == preVictim) {
            return;
        }
        passiveState.setAddition(target);
        final long addDmg = (long) (param.getDmgUpRate() * damage);
        context.addPassiveValue(target, AlterType.HP, addDmg);
        skillReport.add(time, target.getId(), PassiveValue.single(passiveState.getId(), owner.getId(), Hp.of(addDmg)));

        passiveState.addCD(time);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.DMG_UP_ON_TARGET_SHIFT;
    }
}
