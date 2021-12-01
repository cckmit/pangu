package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.UnitRate;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackBeforePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillReleasePassive;
import com.pangu.logic.module.battle.service.passive.param.XingChenSheShouZSParam;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 星辰射手·莎凡娜专属装备
 * 1：自身存活时，提升队伍10%命中率
 * 10：自身存活时，提升队伍15%命中率
 * 20：己方力量、敏捷英雄，每比敌方多1%命中，则提升0.3%伤害，最高15%
 * 30：自身存活时，提升队伍25%命中率
 * @author Kubby
 */
@Component
public class XingChenSheShouZS implements SkillReleasePassive, AttackBeforePassive {

    @Override
    public PassiveType getType() {
        return PassiveType.XING_CHEN_SHE_SHOU_ZS;
    }

    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time,
                             Context context, SkillReport skillReport) {
        if (owner == attacker) {
            return;
        }
        if (owner.getFriend() != attacker.getFriend()) {
            return;
        }
        if (owner.isDead()) {
            return;
        }
        if (attacker.getPassiveStates(passiveState.getId()) != null) {
            return;
        }


        if (passiveState.getAddition(XingChenSheShouZSAddition.class) == null) {
            XingChenSheShouZSAddition addition = new XingChenSheShouZSAddition(owner);
            passiveState.setAddition(addition);
        }


        attacker.addPassive(passiveState, owner);
    }

    @Override
    public void attackBefore(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time,
                             Context context, SkillReport skillReport) {
        XingChenSheShouZSAddition addition = passiveState.getAddition(XingChenSheShouZSAddition.class);
        if (addition == null) {
            return;
        }
        Unit passiveOwner = addition.getOwner();
        if (passiveOwner.isDead()) {
            return;
        }

        XingChenSheShouZSParam param = passiveState.getParam(XingChenSheShouZSParam.class);

        double hitUpRate = param.getHitUpRate();

        owner.increaseRate(UnitRate.HIT, hitUpRate);
        addition.recordChangeValue(owner, UnitRate.HIT, hitUpRate);

        if (param.getHarmUpProfessions().contains(owner.getProfession())) {

            double ownerHitRate = owner.getRate(UnitRate.HIT);
            double targetHitRate = target.getRate(UnitRate.HIT);

            double hitRateSpan = ownerHitRate - targetHitRate;
            if (hitRateSpan > 0) {
                double calcHarmUpRate = hitRateSpan / 0.01 * param.getHarmUpRate();
                double finalHarmUpRate = Math.min(calcHarmUpRate, param.getHarmUpLimit());

                if (finalHarmUpRate > 0) {
                    owner.increaseRate(UnitRate.HARM_P, finalHarmUpRate);
                    owner.increaseRate(UnitRate.HARM_M, finalHarmUpRate);
                    addition.recordChangeValue(owner, UnitRate.HARM_P, finalHarmUpRate);
                    addition.recordChangeValue(owner, UnitRate.HARM_M, finalHarmUpRate);
                }
            }
        }
    }

    @Override
    public void attackEnd(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time,
                          Context context, SkillReport skillReport) {
        XingChenSheShouZSAddition addition = passiveState.getAddition(XingChenSheShouZSAddition.class);
        if (addition == null) {
            return;
        }

        Map<UnitRate, Double> changeValues = addition.removeChangeValue(owner);
        if (changeValues == null || changeValues.isEmpty()) {
            return;
        }
        for (Map.Entry<UnitRate, Double> entry : changeValues.entrySet()) {
            UnitRate unitRate = entry.getKey();
            double value = entry.getValue();
            owner.increaseRate(unitRate, -value);
        }
    }

    @Getter
    private class XingChenSheShouZSAddition {

        private Unit owner;

        private Map<Unit, Map<UnitRate, Double>> changeValues = new HashMap<>();

        public XingChenSheShouZSAddition(Unit owner) {
            this.owner = owner;
        }

        public void recordChangeValue(Unit unit, UnitRate unitRate, double value) {
            changeValues.computeIfAbsent(unit, key -> new HashMap<>()).merge(unitRate, value, Double::sum);
        }

        public Map<UnitRate, Double> removeChangeValue(Unit unit) {
            return changeValues.remove(unit);
        }

    }
}
