package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("PASSIVE:ZiRanZhiNu")
public class ZiRanZhiNu implements AttackPassive {
    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (damage >= 0) {
            return;
        }
        if (skillState.getEffectStates().get(0).getType() != EffectType.ZI_RAN_ZHI_NU) return;
        final Map<Unit, Integer> atkMap = (Map<Unit, Integer>) context.getRootSkillEffectAction().getAddition();
        final Integer atkTimesInThisCircle = atkMap.get(target);
        final Double param = passiveState.getParam(Double.class);
        final long dmgIncrease = (long) (damage * (atkTimesInThisCircle -1) * param);
        context.addPassiveValue(target, AlterType.HP, dmgIncrease);
        skillReport.add(time, target.getId(), PassiveValue.single(passiveState.getId(), owner.getId(), Hp.of(dmgIncrease)));
        passiveState.addCD(time);
    }
    @Override
    public PassiveType getType() {
        return PassiveType.ZI_RAN_ZHI_NU;
    }
}
