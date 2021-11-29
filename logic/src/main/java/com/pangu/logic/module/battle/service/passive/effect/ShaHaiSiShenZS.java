package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillReleasePassive;
import com.pangu.logic.module.battle.service.passive.param.ShaHaiSiShenZSParam;
import com.pangu.logic.module.battle.service.skill.effect.MpChange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ShaHaiSiShenZS implements SkillReleasePassive {
    @Autowired
    private MpChange mpChangeEffect;

    @Override
    public PassiveType getType() {
        return PassiveType.SHA_HAI_SI_SHEN_ZS;
    }

    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time, Context context, SkillReport skillReport) {
        if (attacker.getFriend() != owner.getFriend() || skillState.getType() != SkillType.SPACE || owner == attacker) {
            return;
        }
        final ShaHaiSiShenZSParam param = passiveState.getParam(ShaHaiSiShenZSParam.class);
        final long hpRecover = (long) (param.getRecoverHpPct() * owner.getValue(UnitValue.HP_MAX));
        final double hpPct = owner.getHpPct();
        context.addValue(owner, AlterType.HP, hpRecover);
        final String ownerId = owner.getId();
        skillReport.add(time, ownerId, PassiveValue.single(passiveState.getId(), ownerId, Hp.of(hpRecover)));
        final double hpPctOverflow = param.getRecoverHpPct() - (1.0 - hpPct);
        if (hpPctOverflow <= 0) {
            return;
        }
        final Integer mpChange = (int) (hpPctOverflow * 100 * param.getMpPerHpPctOverFlow());
        final EffectState effectState = new EffectState(null, 0);
        effectState.setParamOverride(mpChange);
        mpChangeEffect.execute(effectState, owner, owner, skillReport, time, skillState, context);
    }
}
