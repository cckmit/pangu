package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.BuffReport;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.resource.BuffSetting;
import com.pangu.logic.module.battle.resource.FightSkillSetting;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.UnitHpChangePassive;
import com.pangu.logic.module.battle.service.passive.param.CureUpParam;
import com.pangu.logic.utils.ExpressionHelper;
import com.pangu.framework.utils.reflect.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 所有治疗效果对生命值低于30%的角色提高50%
 */
@Component
public class CureUp implements UnitHpChangePassive {
    @Override
    public PassiveType getType() {
        return PassiveType.ACTIVE_CURE_UP;
    }

    @Override
    public void hpChange(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> changeUnit) {
        if (attacker != owner) {
            return;
        }

        final CureUpParam param = passiveState.getParam(CureUpParam.class);
        if (damageReport instanceof SkillReport) {
            final String skillId = ((SkillReport) damageReport).getSkillId();
            final String prefix = FightSkillSetting.getPrefix(skillId);
            if (!param.getSkillIdPrefixes().contains(prefix)) {
                return;
            }
        } else if (damageReport instanceof BuffReport) {
            final String buffId = ((BuffReport) damageReport).getBuffId();
            final String prefix = BuffSetting.getPrefix(buffId);
            if (!param.getBuffIdPrefixes().contains(prefix)) {
                return;
            }
        }

        final double cureUpRate = param.getCureUpRate();
        final String subCond = param.getSubCond();
        final Map<String, Object> ctx = new HashMap<>();
        ctx.put("owner", owner);
        for (Unit unit : changeUnit) {
            ctx.put("target", unit);
            if (!StringUtils.isEmpty(subCond) && !ExpressionHelper.invoke(subCond, boolean.class, ctx)) {
                continue;
            }

            final long actualHpChange = context.getActualHpChange(unit);
            final long cureUp = (long) (actualHpChange * cureUpRate);
            context.passiveRecover(owner, unit, cureUp, time, passiveState, damageReport);
        }
    }
}
