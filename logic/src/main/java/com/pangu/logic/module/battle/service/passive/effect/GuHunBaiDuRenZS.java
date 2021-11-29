package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.FilterType;
import com.pangu.logic.module.battle.model.UnitRate;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Mp;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.alter.MpAlter;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.*;
import com.pangu.logic.module.battle.service.passive.param.GuHunBaiDuRenZSParam;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 孤魂摆渡人·拜尔斯专属装备
 * 1：根据自身能量，能量越多，治疗量增加越多，最多30%额外治疗（公式：每100点能量增加XX%治疗率）
 * 10：对队友的治疗每累计队友的最大生命值15%，自身获得40点能量，每个英雄独立计算
 * 20：对队友的治疗每累计队友的最大生命值13%，自身获得50点能量，每个英雄独立计算
 * 30：对队友的治疗每累计队友的最大生命值10%，自身获得60点能量，每个英雄独立计算
 *
 * @author Kubby
 */
@Component
public class GuHunBaiDuRenZS implements AttackBeforePassive, UnitHpChangePassive {

    @Override
    public void attackBefore(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time,
                             Context context, SkillReport skillReport) {
        GuHunBaiDuRenZSParam param = passiveState.getParam(GuHunBaiDuRenZSParam.class);

        double calcCureUpRate = owner.getValue(UnitValue.MP) / 100.0 * param.getCureUpRate();
        double finalCureUpRate = Math.min(calcCureUpRate, param.getCureUpLimit());

        owner.increaseRate(UnitRate.CURE, finalCureUpRate);

        final Addition addition = passiveState.getAddition(Addition.class, new Addition());
        addition.finalCureUpRate = finalCureUpRate;
    }

    @Override
    public void attackEnd(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time,
                          Context context, SkillReport skillReport) {
        final Addition addition = passiveState.getAddition(Addition.class, new Addition());
        double finalCureUpRate = addition.finalCureUpRate;
        owner.increaseRate(UnitRate.CURE, -finalCureUpRate);

        addition.finalCureUpRate = 0;
    }


    @Override
    public void hpChange(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> changeUnit) {
        final GuHunBaiDuRenZSParam param = passiveState.getParam(GuHunBaiDuRenZSParam.class);
        final double mpUpHpPctCondition = param.getMpUpHpPct();
        if (mpUpHpPctCondition <= 0) {
            return;
        }

        final List<Unit> changeFriends = changeUnit.stream()
                .filter(FilterType.FRIEND.filter(owner, time)::contains)
                .collect(Collectors.toList());
        final Addition addition = passiveState.getAddition(Addition.class, new Addition());
        final Map<Unit, Long> preUnitHp = addition.preHp;
        final Map<Unit, Double> remainder = addition.remainder;
        PassiveValue passiveValue = null;
        for (Unit changeFriend : changeFriends) {
            final long curHP = changeFriend.getValue(UnitValue.HP);

            //获取上轮hp变化后的值
            Long preHP = preUnitHp.get(changeFriend);
            final long hpMax = changeFriend.getValue(UnitValue.HP_MAX);
            if (hpMax == 0) {
                continue;
            }
            if (preHP == null) {
                preHP = hpMax;
            }

            //缓存每个生命值发生变动的角色当前血量
            preUnitHp.put(changeFriend, curHP);
            //引起生命值变动的施法者非此被动持有者时，不执行任何操作
            if (owner != attacker) {
                continue;
            }

            //施法者执行的效果并非治疗时，不执行任何操作
            final long hpChange = curHP - preHP;
            if (hpChange <= 0) {
                continue;
            }

            //将上轮的零头与本轮造成的效果相加
            final double accRecoverHpPct = remainder.merge(changeFriend, hpChange / 1.0 / hpMax, Double::sum);
            //累计效果足以触发多少次回能奖励
            final double dTimes = accRecoverHpPct / mpUpHpPctCondition;
            final long lTimes = (long) dTimes;
            //本轮的零头
            final double remainderPct = (dTimes - lTimes) * mpUpHpPctCondition;
            //缓存本轮零头
            remainder.put(changeFriend, remainderPct);

            //执行回能奖励
            if (lTimes <= 0) {
                continue;
            }
            final long mpChange = lTimes * param.getMpUpValue();
            final long realMpChange = MpAlter.calMpChange(owner, mpChange);
            if (realMpChange == 0) {
                continue;
            }
            if (passiveValue == null) {
                passiveValue = PassiveValue.of(passiveState.getId(), owner.getId());
            }
            context.addPassiveValue(owner, AlterType.MP, mpChange);
            passiveValue.add(new Mp(realMpChange));
        }
        if (passiveValue != null) {
            damageReport.add(time, owner.getId(), passiveValue);
        }
    }

    private static class Addition {
        final private Map<Unit, Long> preHp = new HashMap<>();
        final private Map<Unit, Double> remainder = new HashMap<>();
        private double finalCureUpRate;
    }

    @Override
    public PassiveType getType() {
        return PassiveType.GU_HUN_BAI_DU_REN_ZS;
    }
}
