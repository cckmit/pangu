package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.BuffReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.service.buff.Buff;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.buff.param.DamageOverTimeParam;
import com.pangu.logic.module.battle.service.buff.param.DamageOverTimeParam.CalType;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.skill.effect.HpMagicDamage;
import com.pangu.logic.module.battle.service.skill.effect.HpPhysicsDamage;
import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import com.pangu.logic.module.common.resource.Formula;
import com.pangu.logic.utils.ExpressionHelper;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 持续性伤害
 *
 * @author Kubby
 */
@Component("BUFF:DamageOverTime")
public class DamageOverTime implements Buff {

    @Autowired
    private HpMagicDamage magicDamage;
    @Autowired
    private HpPhysicsDamage physicsDamage;

    @Override
    public BuffType getType() {
        return BuffType.DAMAGE_OVER_TIME;
    }

    @Override
    public boolean add(BuffState state, Unit unit, int time) {
        unit.addBuff(state);
        DamageOverTimeParam param = state.getParam(DamageOverTimeParam.class);
        if (param.isDmgFixed()) {
            final Long fixedDmg = state.getAddition(Long.class, 0L);
            DamageResult damageResult = calcDamage(state.getCaster(), unit, param, time, fixedDmg);
            state.setAddition(damageResult);
        }
        return true;
    }

    @Override
    public void update(BuffState state, Unit unit, int time, Object o) {
        DamageOverTimeParam param = state.getParam(DamageOverTimeParam.class);
        if (param.getTargetId() == null) param.setTargetId("SELF");
        final List<Unit> targets = TargetSelector.select(unit, param.getTargetId(), time);
        final Unit caster = state.getCaster();
        Context context = new Context(caster);
        BuffReport buffReport = state.getBuffReport();
        for (Unit target : targets) {
            DamageResult damageResult;
            if (param.isDmgFixed()) {
                damageResult = state.getAddition(DamageResult.class);
            } else {
                damageResult = calcDamage(caster, target, param, time, null);
            }

            long damage = damageResult.getDamage();
            boolean crit = damageResult.isCrit();
            boolean block = damageResult.isBlock();

            if (damage == 0) {
                continue;
            }
            context.addValue(target, AlterType.HP, damage);
            buffReport.add(time, target.getId(), new Hp(damage, crit, block));
        }
        context.execute(time, buffReport);
    }

    public DamageResult calcDamage(Unit owner, Unit target, DamageOverTimeParam param, int time, Long fixedDmg) {
        long damage = 0;
        boolean crit = false;
        boolean block = false;

        CalType calType = param.getCalType();

        if (param.isMagic() && magicDamage.canMagicImmune(owner, target, time)) {
            return DamageResult.of(damage, false, false);
        }
        if (!param.isMagic() && physicsDamage.canPhysicsImmune(owner, target, time)) {
            return DamageResult.of(damage, false, false);
        }

        switch (calType) {
            case DYNAMIC:
                damage = fixedDmg;
                break;
            case VALUE:
                damage = ((Number) param.getContent()).longValue();
                break;
            case FORMULA:
                Map<String, Object> formulaParams = new HashMap<>(param.castContent());
                String formulaId = (String) formulaParams.get("formula");
                Formula formula = BuffFactory.getFormula(formulaId);
                Map<String, Object> formulaCtx = new HashMap<>(formulaParams);
                formulaCtx.put("owner", owner);
                formulaCtx.put("target", target);
                damage = ((Number) formula.calculate(formulaCtx)).longValue();
                break;
            case EXP:
                Map<String, Object> exprParams = new HashMap<>(param.castContent());
                String expr = (String) exprParams.get("expr");
                Map<String, Object> exprCtx = new HashMap<>(exprParams);
                exprCtx.put("owner", owner);
                exprCtx.put("target", target);
                damage = ExpressionHelper.invoke(expr, Long.class, exprCtx);
                break;
            case SKILL:
                double factor = param.castContent();
                EffectState effectState = new EffectState(null, 0);
                effectState.setParamOverride(new DamageParam(factor));
                if (param.isMagic()) {
                    HpMagicDamage.MagicDamageCalcResult damageCalcResult = magicDamage
                            .calcDamage(owner, target, null, effectState, time);
                    damage = damageCalcResult.getDamage();
                    crit = damageCalcResult.isCrit();
                } else {
                    HpPhysicsDamage.PhysicsDamageCalcResult damageCalcResult = physicsDamage
                            .calcDamage(owner, target, null, effectState, time);
                    damage = damageCalcResult.getDamage();
                    crit = damageCalcResult.isCrit();
                    block = damageCalcResult.isBlock();
                }
        }

        return DamageResult.of(damage, crit, block);
    }

    @Getter
    private static class DamageResult {

        private long damage;

        private boolean crit;

        private boolean block;

        public static DamageResult of(long damage, boolean crit, boolean block) {
            DamageResult result = new DamageResult();
            result.damage = damage;
            result.crit = crit;
            result.block = block;
            return result;
        }
    }
}
