package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.framework.resource.anno.Static;
import com.pangu.framework.utils.math.RandomUtils;
import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.UnitRate;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.model.report.values.Immune;
import com.pangu.logic.module.battle.resource.BattleSetting;
import com.pangu.logic.module.battle.service.Battle;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.ctx.AttackCtx;
import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import com.pangu.logic.module.common.resource.Formula;
import com.pangu.logic.utils.ExpressionHelper;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 对敌人HP法术伤害技能<br/>
 * 该效果产生的伤害值会被记录在上下文中，作为后续效果执行的基础
 */
@Component
public class HpMagicDamage implements SkillEffect {

    //  是否物理暴击计算公式
    @Static("HP_M_DAMAGE_CRIT_HIT")
    private Formula isCritFormula;
    //  伤害值公式:普通
    @Static("FIGHT:HP:M_DAMAGE:NORMAL")
    private Formula normalFormula;
    //  伤害值公式:普通PVP
    @Static("FIGHT:HP:M_DAMAGE:NORMAL:PVP")
    private Formula normalPVPFormula;
    //  伤害值公式:暴击
    @Static("FIGHT:HP:M_DAMAGE:CRIT")
    private Formula critFormula;
    //  伤害值公式:暴击PVP
    @Static("FIGHT:HP:M_DAMAGE:CRIT:PVP")
    private Formula critPVPFormula;

    @Override
    public EffectType getType() {
        return EffectType.HP_M_DAMAGE;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        // 是否免疫法术攻击
        if (canMagicImmune(owner, target, time)) {
            skillReport.add(time, target.getId(), new Immune());
            return;
        }

        MagicDamageCalcResult damageCalcResult = calcDamage(owner, target, skillState, state, time);

        long damage = damageCalcResult.getDamage();
        boolean isCrit = damageCalcResult.isCrit();

        context.addValue(target, AlterType.HP, damage);
        context.setCrit(target, isCrit);
        context.setMagic(target);

        skillReport.add(time, target.getId(), new Hp(damage, isCrit, false));
    }

    private double getAttackAddRate(Unit owner, SkillState skillState) {
        double skillAttackAddRate = 0.0;
        if (skillState != null) {
            switch (skillState.getType()) {
                case NORMAL:
                    skillAttackAddRate = owner.getRate(UnitRate.NORMAL_SKILL_ADD);
                    break;
                case SKILL:
                    skillAttackAddRate = owner.getRate(UnitRate.SKILL_ADD);
                    break;
                case SPACE:
                    skillAttackAddRate = owner.getRate(UnitRate.SPACE_ADD);
                    break;
            }
        }
        return skillAttackAddRate;
    }

    public boolean canMagicImmune(Unit owner, Unit target, int time) {
        if (target.hasState(UnitState.MAGIC_IMMUNE, time)) {
            return !owner.hasState(UnitState.IMMUNE_MAGIC_IMMUNE, time);
        }
        return false;
    }

    public MagicDamageCalcResult calcDamage(Unit owner, Unit target, SkillState skillState, EffectState effectState, int time) {
        DamageParam damageParam = effectState.getParam(DamageParam.class);
        return calcDamage(owner, target, skillState, damageParam, time);
    }

    public MagicDamageCalcResult calcDamage(Unit owner, Unit target, SkillState skillState, DamageParam damageParam, int time) {
        double attackAddRate = getAttackAddRate(owner, skillState);
        AttackCtx ctx = new AttackCtx(time, owner, target, damageParam, attackAddRate);

        boolean isCrit;
        if (StringUtils.isNotEmpty(damageParam.getCritExp())) {
            isCrit = ExpressionHelper.invoke(damageParam.getCritExp(), Boolean.class, ctx);
        } else {
            double rate = (Double) isCritFormula.calculate(ctx);
            isCrit = RandomUtils.isHit(rate);
        }

        Formula damageFormula;
        Battle battle = owner.getBattle();
        BattleSetting battleSetting = battle.getConfig();
        boolean pvp = battleSetting.isPvp();

        if (isCrit) {
            damageFormula = pvp ? critPVPFormula : critFormula;
        } else {
            damageFormula = pvp ? normalPVPFormula : normalFormula;
        }

        long damage = (Long) damageFormula.calculate(ctx);
        return MagicDamageCalcResult.of(damage, isCrit);
    }

    @Getter
    public static class MagicDamageCalcResult {

        private long damage;

        private boolean crit;

        public static MagicDamageCalcResult of(long damage, boolean crit) {
            MagicDamageCalcResult result = new MagicDamageCalcResult();
            result.damage = damage;
            result.crit = crit;
            return result;
        }
    }

}