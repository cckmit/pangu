package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackBeforePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.MoChuanParam;
import com.pangu.framework.utils.math.RandomUtils;
import org.springframework.stereotype.Component;

/**
 * 魔穿(3级)
 * 战斗中，攻击有10%几率无视敌人的物理防御，对拥有护盾的敌人额外造成100%的伤害。
 * 2级:无视防御力的几率提升至20%
 * 3级:对有护盾的敌人额外造成200%的伤害
 * 4级：无视防御力的几率提升至30%
 */
@Component
public class MoChuan implements AttackBeforePassive {
    @Override
    public void attackBefore(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time, Context context, SkillReport skillReport) {
        MoChuanValue additon = getAddition(passiveState);
        additon.valid = false;
        if (owner.getFriend() == target.getFriend()) {
            return;
        }
        EffectType effectType = effectState.getType();
        if (effectType != EffectType.HP_P_DAMAGE && effectType != EffectType.HP_M_DAMAGE) {
            return;
        }
        MoChuanParam param = passiveState.getParam(MoChuanParam.class);
        double ignoreDefenceRate = param.getIgnoreDefenceRate();
        if (!RandomUtils.isHit(ignoreDefenceRate)) {
            return;
        }
        additon.valid = true;
        long defenceM = target.getValue(UnitValue.DEFENCE_M);
        long defenceP = target.getValue(UnitValue.DEFENCE_P);
        target.setValue(UnitValue.DEFENCE_M, 0);
        target.setValue(UnitValue.DEFENCE_P, 0);
        additon.defenceM = defenceM;
        additon.defenceP = defenceP;
    }

    private MoChuanValue getAddition(PassiveState passiveState) {
        MoChuanValue addition = passiveState.getAddition(MoChuanValue.class);
        if (addition == null) {
            addition = new MoChuanValue();
            passiveState.setAddition(addition);
        }
        return addition;
    }

    @Override
    public void attackEnd(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time, Context context, SkillReport skillReport) {
        MoChuanValue addition = getAddition(passiveState);
        if (!addition.valid) {
            return;
        }
        addition.valid = false;
        target.setValue(UnitValue.DEFENCE_M, addition.defenceM);
        target.setValue(UnitValue.DEFENCE_P, addition.defenceP);
        long hpChange = context.getOriginHp(target);
        if (hpChange >= 0) {
            return;
        }
        if (target.getValue(UnitValue.SHIELD)<=0) {
            return;
        }
        MoChuanParam param = passiveState.getParam(MoChuanParam.class);
        long deepen = (long) (hpChange * param.getDamageDeepen());
        if (deepen == 0) {
            return;
        }
        context.addPassiveValue(target, AlterType.HP, deepen);
        skillReport.add(time, target.getId(), PassiveValue.single(passiveState.getId(), owner.getId(), Hp.of(deepen)));
    }

    @Override
    public PassiveType getType() {
        return PassiveType.MO_CHUAN;
    }

    private static class MoChuanValue {
        private boolean valid;
        private long defenceM;
        private long defenceP;
    }
}
