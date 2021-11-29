package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.UnitRate;
import com.pangu.logic.module.battle.model.UnitType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.resource.FightSkillSetting;
import com.pangu.logic.module.battle.service.buff.utils.CalTypeHelper;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.*;
import com.pangu.logic.module.battle.service.passive.param.SkillAttributeAlterParam;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.utils.ExpressionHelper;
import com.pangu.framework.resource.Storage;
import com.pangu.framework.resource.anno.Static;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 技能属性临时变更
 *
 * @author Kubby
 */
@Component
public class SkillAttributeAlter implements AttackBeforePassive, BeAttackBeforePassive {

    @Static
    private Storage<String, FightSkillSetting> skillStorage;

    @Override
    public PassiveType getType() {
        return PassiveType.SKILL_ATTR_ALTER;
    }

    @Override
    public void attackBefore(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time,
                             Context context, SkillReport skillReport) {
        SkillAttributeAlterParam param = passiveState.getParam(SkillAttributeAlterParam.class);
        if (param.getPhase() == Phase.ATTACK_BEFORE) {
            inc(passiveState, owner, target, skillReport, effectState, time, context);
        }
    }

    @Override
    public void attackEnd(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time,
                          Context context, SkillReport skillReport) {
        SkillAttributeAlterParam param = passiveState.getParam(SkillAttributeAlterParam.class);
        if (param.getPhase() == Phase.ATTACK_BEFORE) {
            dec(passiveState, owner, target);
        }
    }

    @Override
    public void beAttackBefore(PassiveState passiveState, EffectState effectState, Unit owner, Unit attacker, int time,
                               Context context, SkillReport skillReport) {
        SkillAttributeAlterParam param = passiveState.getParam(SkillAttributeAlterParam.class);
        if (param.getPhase() == Phase.BE_ATTACK_BEFORE) {
            inc(passiveState, owner, attacker, skillReport, effectState, time, context);
        }
    }

    @Override
    public void beAttackEnd(PassiveState passiveState, EffectState effectState, Unit owner, Unit attacker, int time,
                            Context context, SkillReport skillReport) {
        SkillAttributeAlterParam param = passiveState.getParam(SkillAttributeAlterParam.class);
        if (param.getPhase() == Phase.BE_ATTACK_BEFORE) {
            dec(passiveState, owner, attacker);
        }
    }

    private void inc(PassiveState passiveState, Unit owner, Unit target, SkillReport skillReport,
                     EffectState effectState, int time, Context context) {
        SkillAttributeAlterParam param = passiveState.getParam(SkillAttributeAlterParam.class);

        SkillAttributeAlterContext conditionContext = new SkillAttributeAlterContext(owner, target, effectState,
                skillReport, time, context);

        if (!StringUtils.isBlank(param.getCondition())) {
            boolean ok = ExpressionHelper.invoke(param.getCondition(), boolean.class, conditionContext);
            if (!ok) {
                return;
            }
        }

        Unit alterUnit = param.isAlterOwner() ? owner : target;

        SkillAttributeAlterAddition addition = passiveState
                .getAddition(SkillAttributeAlterAddition.class, new SkillAttributeAlterAddition());

        addition.clear();

        if (!param.getValues().isEmpty()) {
            Map<UnitValue, Long> valueAlters = CalTypeHelper
                    .calUnitValues(param.getCalType(), param.getValues(), owner, target, 0, conditionContext);

            for (Map.Entry<UnitValue, Long> entry : valueAlters.entrySet()) {
                UnitValue k = entry.getKey();
                Long v = entry.getValue();
                long oldValue = alterUnit.getValue(k);
                long currValue = alterUnit.increaseValue(k, v);
                long realAlter = currValue - oldValue;
                addition.valueAlters.put(k, realAlter);
            }
        }

        if (!param.getRates().isEmpty()) {
            Map<UnitRate, Double> rateAlters = CalTypeHelper
                    .calUnitRates(param.getCalType(), param.getRates(), owner, target, time, conditionContext);

            for (Map.Entry<UnitRate, Double> entry : rateAlters.entrySet()) {
                UnitRate k = entry.getKey();
                Double v = entry.getValue();
                double oldValue = alterUnit.getRate(k);
                double currValue = alterUnit.increaseRate(k, v);
                double realAlter = currValue - oldValue;
                addition.rateAlters.put(k, realAlter);
            }
        }
    }

    private void dec(PassiveState passiveState, Unit owner, Unit target) {
        SkillAttributeAlterParam param = passiveState.getParam(SkillAttributeAlterParam.class);
        Unit alterUnit = param.isAlterOwner() ? owner : target;

        SkillAttributeAlterAddition addition = passiveState
                .getAddition(SkillAttributeAlterAddition.class, new SkillAttributeAlterAddition());

        if (!addition.valueAlters.isEmpty()) {
            addition.valueAlters.forEach((k, v) -> {
                alterUnit.increaseValue(k, -v);
            });
        }

        if (!addition.rateAlters.isEmpty()) {
            addition.rateAlters.forEach((k, v) -> {
                alterUnit.increaseRate(k, -v);
            });
        }

        addition.clear();
    }

    public static class SkillAttributeAlterAddition {

        private Map<UnitValue, Long> valueAlters = new HashMap<>();
        private Map<UnitRate, Double> rateAlters = new HashMap<>();

        void clear() {
            valueAlters.clear();
            rateAlters.clear();
        }
    }

    @Getter
    public class SkillAttributeAlterContext {

        private Unit owner;

        private Unit target;

        private EffectState effectState;

        private SkillReport skillReport;

        private int time;

        private Context context;

        public SkillAttributeAlterContext(Unit owner, Unit target, EffectState effectState,
                                          SkillReport skillReport, int time, Context context) {
            this.owner = owner;
            this.target = target;
            this.effectState = effectState;
            this.skillReport = skillReport;
            this.time = time;
            this.context = context;
        }

        public boolean ownerIsUnitType(String typeStr) {
            UnitType unitType = UnitType.valueOf(typeStr);
            return owner.getProfession().contains(unitType);
        }

        public boolean targetIsUnitType(String typeStr) {
            UnitType unitType = UnitType.valueOf(typeStr);
            return target.getProfession().contains(unitType);
        }

        public boolean ownerHasUsefulState() {
            return !owner.getStates(false, time).isEmpty();
        }

        public int getSelectCount(String selectId) {
            return TargetSelector.select(owner, selectId, time).size();
        }

        public boolean isSkillId(String skillId) {
            FightSkillSetting skillSetting = skillStorage.get(skillReport.getSkillId(), true);
            return skillSetting.getId().equals(skillId);
        }

        public boolean isSkillTag(String skillTag) {
            FightSkillSetting skillSetting = skillStorage.get(skillReport.getSkillId(), true);
            return skillSetting.getTag() != null && skillSetting.getTag().equals(skillTag);
        }

        public boolean isSkillType(String typeStr) {
            SkillType skillType = SkillType.valueOf(typeStr);
            FightSkillSetting skillSetting = skillStorage.get(skillReport.getSkillId(), true);
            return skillSetting.getType() == skillType;
        }

        public boolean ownerHasBuffByTag(String tag) {
            return owner.getBuffStateByTag(tag) != null;
        }

        public boolean targetHasBuffByTag(String tag) {
            return target.getBuffStateByTag(tag) != null;
        }

        public int getFriendDeadCount() {
            return owner.getFriend().getDieUnit().size();
        }

        public int getEnemyDeadCount() {
            return owner.getEnemy().getDieUnit().size();
        }

        public int getTargetAmount() {
            return context.getTargetAmount();
        }
    }

}
