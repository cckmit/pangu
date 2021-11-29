package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 造成物理或法术攻击中较高一方的伤害<br/>
 */
@Component
public class HpHigherDamage implements SkillEffect {
    @Autowired
    private HpMagicDamage magicDamage;
    @Autowired
    private HpPhysicsDamage physicsDamage;

    @Override
    public EffectType getType() {
        return EffectType.HP_H_DAMAGE;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final HigherDmgResult res = calcHigherDmgRes(owner, target, skillState, state.getParam(DamageParam.class), time);
        long damage = res.getDamage();
        boolean isCrit = res.isCrit();
        context.addValue(target, AlterType.HP, damage);
        context.setCrit(target, isCrit);
        if (res.isMagic()) {
            context.setMagic(target);
        }
        skillReport.add(time, target.getId(), new Hp(damage, isCrit, res.isBlock()));
    }

    public HigherDmgResult calcHigherDmgRes(Unit owner, Unit target, SkillState skillState, DamageParam dmgParam, int time) {
        final UnitValue atkValue = owner.getValue(UnitValue.ATTACK_M) > owner.getValue(UnitValue.ATTACK_P) ? UnitValue.ATTACK_M : UnitValue.ATTACK_P;
        if (atkValue == UnitValue.ATTACK_M) {
            final HpMagicDamage.MagicDamageCalcResult res = magicDamage.calcDamage(owner, target, skillState, dmgParam, time);
            return HigherDmgResult.fromMagDmg(res.getDamage(), res.isCrit(), false);
        } else {
            final HpPhysicsDamage.PhysicsDamageCalcResult res = physicsDamage.calcDamage(owner, target, skillState, dmgParam, time);
            return HigherDmgResult.fromPhyDmg(res.getDamage(), res.isCrit(), res.isBlock());
        }
    }

    @Getter
    public static class HigherDmgResult {
        private long damage;

        private boolean crit;

        private boolean block;

        private boolean magic;

        public static HigherDmgResult fromPhyDmg(long damage, boolean crit, boolean block) {
            HigherDmgResult result = new HigherDmgResult();
            result.damage = damage;
            result.crit = crit;
            result.block = block;
            return result;
        }

        public static HigherDmgResult fromMagDmg(long damage, boolean crit, boolean block) {
            HigherDmgResult result = new HigherDmgResult();
            result.damage = damage;
            result.crit = crit;
            result.block = block;
            result.magic = true;
            return result;
        }
    }
}
