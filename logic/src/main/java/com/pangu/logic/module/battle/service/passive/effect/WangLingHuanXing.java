package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.UnitDiePassive;
import com.pangu.logic.module.battle.service.passive.param.WangLingHuanXingParam;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import com.pangu.framework.utils.math.RandomUtils;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 亡灵唤醒
 * 在攻击时有15%的几率造成额外50%的伤害。每参与击杀2个敌方时，会召唤1个骷髅兵（拥有骷髅王80%的属性，受到400%的伤害加深）。
 * 2级：额外伤害触发率提升至25%
 * 3级：骷髅兵被击杀后可以重生一次
 * 4级：额外伤害触发率提升至35%
 */
@Component
public class WangLingHuanXing implements AttackPassive, UnitDiePassive {

    @Override
    public PassiveType getType() {
        return PassiveType.WANG_LING_HUAN_XING;
    }

    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        WangLingHuanXingValue addition = getAddition(passiveState);
        addition.damageUnitIds.add(target.getId());
        WangLingHuanXingParam param = passiveState.getParam(WangLingHuanXingParam.class);
        if (param.getDamageHitRate() <= 0 || !RandomUtils.isHit(param.getDamageHitRate())) {
            return;
        }
        long enhance = (long) (damage * param.getDamageEnhance());
        skillReport.add(time, target.getId(), PassiveValue.single(passiveState.getId(), owner.getId(), Hp.of(enhance)));
    }

    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> dieUnits) {
        WangLingHuanXingValue addition = getAddition(passiveState);
        int dieAmount = addition.preKilled;
        if (owner == attacker) {
            dieAmount += dieUnits.size();
        } else {
            Set<String> ids = addition.damageUnitIds;
            for (Unit dieUnit : dieUnits) {
                if (ids.contains(dieUnit.getId())) {
                    ++dieAmount;
                }
            }
        }
        WangLingHuanXingParam param = passiveState.getParam(WangLingHuanXingParam.class);
        int repeat = dieAmount / param.getSummonUnitPerDie();
        addition.preKilled = dieAmount % param.getSummonUnitPerDie();

        if (repeat <= 0) {
            return;
        }

        String skillId = param.getSkillId();
        SkillState skillState = SkillFactory.initState(skillId);
        for (int i = 0; i < repeat; ++i) {
            List<EffectState> effectStates = skillState.getEffectStates();
            for (EffectState effectState : effectStates) {
                SkillEffect skillEffect = SkillFactory.getSkillEffect(effectState.getType());
                SkillReport report = new SkillReport();
                skillEffect.execute(effectState, owner, owner, report, time, skillState, context);
                damageReport.mergeDamages(report);
            }
        }
    }

    private WangLingHuanXingValue getAddition(PassiveState passiveState) {
        WangLingHuanXingValue addition = passiveState.getAddition(WangLingHuanXingValue.class);
        if (addition == null) {
            addition = new WangLingHuanXingValue();
            passiveState.setAddition(addition);
        }
        return addition;
    }

    private static class WangLingHuanXingValue {
        private int preKilled;

        private final Set<String> damageUnitIds = new HashSet<>(6);

    }
}
