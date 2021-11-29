package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.MpFrom;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Mp;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.MpShieldParam;
import com.pangu.logic.module.battle.service.passive.utils.SkillReportEditor;
import org.springframework.stereotype.Component;

/**
 * 能量抵扣伤害
 */
@Component
public class MpShield implements DamagePassive {
    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final MpShieldParam param = passiveState.getParam(MpShieldParam.class);
        long totalDamage = context.getHpChange(owner);
        if (totalDamage >= 0) {
            return;
        }
        if (-totalDamage / 1D / owner.getValue(UnitValue.HP_MAX) < param.getTriggerRate()) {
            return;
        }
        //计算实际扣减的蓝量
        final long curMp = owner.getValue(UnitValue.MP);
        final long costMp = Math.min(curMp, param.getMaxMpShield());

        long increaseHp = (long) -(costMp / 1D / param.getMaxMpShield() * totalDamage);
        //扣减蓝量
        context.addPassiveValue(owner, AlterType.MP, -costMp);
        skillReport.add(time, owner.getId(), new Mp(-costMp, MpFrom.SKILL));
        //抵消伤害
        context.addPassiveValue(owner, AlterType.HP, increaseHp);
        SkillReportEditor.editHpDamageReport(skillReport, owner, increaseHp, time);
        //传被动id供前端显示受击特效
        skillReport.add(time, owner.getId(), PassiveValue.of(passiveState.getId(), owner.getId()));
        passiveState.addCD(time);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.MP_SHIELD;
    }
}
