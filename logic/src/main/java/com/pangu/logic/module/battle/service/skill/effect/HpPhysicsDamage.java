package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.EffectType;
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
import com.pangu.framework.resource.anno.Static;
import com.pangu.framework.utils.math.RandomUtils;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

/**
 * 对敌人HP物理伤害技能<br/>
 * 该效果产生的伤害值会被记录在上下文中，作为后续效果执行的基础
 */
@Component
public class HpPhysicsDamage implements SkillEffect {

    //  是否物理暴击计算公式
    @Static("HP_P_DAMAGE_CRIT_HIT")
    private Formula isCritFormula;
    //  是否物理暴击计算公式
    @Static("FIGHT:BLOCK:IS_SUCCESS")
    private Formula isBlockMFormula;
    //  伤害值公式:普通
    @Static("FIGHT:HP:P_DAMAGE:NORMAL")
    private Formula normalFormula;
    //  伤害值公式:普通PVP
    @Static("FIGHT:HP:P_DAMAGE:NORMAL:PVP")
    private Formula normalPVPFormula;
    //  伤害值公式:暴击
    @Static("FIGHT:HP:P_DAMAGE:CRIT")
    private Formula critFormula;
    //  伤害值公式:暴击PVP
    @Static("FIGHT:HP:P_DAMAGE:CRIT:PVP")
    private Formula critPVPFormula;
    //  格挡伤害转换
    @Static("FIGHT:BLOCK:HP")
    private Formula blockFormula;

    @Override
    public EffectType getType() {
        return EffectType.HP_P_DAMAGE;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        // 是否免疫物理攻击
        if (canPhysicsImmune(owner, target, time)) {
            skillReport.add(time, target.getId(), new Immune());
            return;
        }

        PhysicsDamageCalcResult damageCalcResult = calcDamage(owner, target, skillState, state, time);

        long damage = damageCalcResult.getDamage();
        boolean isCrit = damageCalcResult.isCrit();
        boolean isBlock = damageCalcResult.isBlock();

        context.addValue(target, AlterType.HP, damage);
        context.setCrit(target, isCrit);

        skillReport.add(time, target.getId(), new Hp(damage, isCrit, isBlock));
    }


    private double getAttackAddRate(com.pangu.logic.module.battle.service.core.Unit owner, SkillState skillState) {
        double skillAttackAddRate = 0.0;
        if (skillState != null) {
            switch (skillState.getType()) {
                case NORMAL:
                    skillAttackAddRate = owner.getRate(com.pangu.logic.module.battle.model.UnitRate.NORMAL_SKILL_ADD);
                    break;
                case SKILL:
                    skillAttackAddRate = owner.getRate(com.pangu.logic.module.battle.model.UnitRate.SKILL_ADD);
                    break;
                case SPACE:
                    skillAttackAddRate = owner.getRate(com.pangu.logic.module.battle.model.UnitRate.SPACE_ADD);
                    break;
            }
        }
        return skillAttackAddRate;
    }

    public boolean canPhysicsImmune(com.pangu.logic.module.battle.service.core.Unit owner, com.pangu.logic.module.battle.service.core.Unit target, int time) {
        if (target.hasState(UnitState.PHYSICS_IMMUNE, time)) {
            return !owner.hasState(UnitState.IMMUNE_PHYSICS_IMMUNE, time);
        }
        return false;
    }

    public PhysicsDamageCalcResult calcDamage(Unit owner, Unit target, SkillState skillState, EffectState effectState, int time) {
        DamageParam damageParam = effectState.getParam(DamageParam.class);
        return calcDamage(owner, target, skillState, damageParam, time);
    }

    public PhysicsDamageCalcResult calcDamage(Unit owner, Unit target, SkillState skillState, DamageParam damageParam, int time) {
        double attackAddRate = getAttackAddRate(owner, skillState);
        AttackCtx ctx = new AttackCtx(time, owner, target, damageParam, attackAddRate);

        boolean isCrit;
        if (StringUtils.isNotEmpty(damageParam.getCritExp())) {
            isCrit = ExpressionHelper.invoke(damageParam.getCritExp(), Boolean.class, ctx);
        } else {
            double rate = (Double) isCritFormula.calculate(ctx);
            isCrit = RandomUtils.isHit(rate);
        }

        Battle battle = owner.getBattle();
        BattleSetting battleSetting = battle.getConfig();
        boolean pvp = battleSetting.isPvp();

        Formula damageFormula;
        if (isCrit) {
            damageFormula = pvp ? critPVPFormula : critFormula;
        } else {
            damageFormula = pvp ? normalPVPFormula : normalFormula;
        }
        long damage = (Long) damageFormula.calculate(ctx);

        double block = (double) isBlockMFormula.calculate(ctx);
        boolean isBlock = RandomUtils.isHit(block);
        if (isBlock) {
            Map<String, Long> blockCtx = Collections.singletonMap("value", damage);
            damage = (long) blockFormula.calculate(blockCtx);
        }

        return PhysicsDamageCalcResult.of(damage, isCrit, isBlock);
    }

    @Getter
    public static class PhysicsDamageCalcResult {

        private long damage;

        private boolean crit;

        private boolean block;

        public static PhysicsDamageCalcResult of(long damage, boolean crit, boolean block) {
            PhysicsDamageCalcResult result = new PhysicsDamageCalcResult();
            result.damage = damage;
            result.crit = crit;
            result.block = block;
            return result;
        }
    }

}