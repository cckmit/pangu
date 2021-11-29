package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.utils.SkillReportEditor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("PASSIVE:ShengMingShouGe")
public class ShengMingShouGe implements AttackPassive {
    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (skillState.getType() != SkillType.SPACE || damage >= 0) return;

        final Double param = passiveState.getParam(Double.class);
        Set<Unit> attackedUnits = context.getRootSkillEffectAction().getAddition(Set.class);
        if (attackedUnits == null) {
            attackedUnits = new HashSet<>();
            context.getRootSkillEffectAction().setAddition(attackedUnits);
        }
        if (attackedUnits.contains(target)) {
            final long dmgDecrease = -(long) ((1 - param) * damage);
            context.addPassiveValue(target, AlterType.HP, dmgDecrease);
            //此处需要修改之前技能的伤害战报，实属奇技淫巧
            SkillReportEditor.editHpDamageReport(skillReport,target,dmgDecrease,time);
        }
        //记录已命中单位
        attackedUnits.add(target);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.SHENG_MING_SHOU_GE;
    }
}
