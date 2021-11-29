package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.FilterType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import com.pangu.logic.module.battle.service.skill.param.HunLiJiQuParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 魂力汲取(4级)
 * 抽取敌人的灵魂能量用于治疗，对附近敌人造成100%攻击力的伤害，并将总伤害的40%作为治疗量平均分配给自己与附近友军
 * 2级:总伤害的50%将被用于治疗
 * 3级:总伤害的60%将被用于治疗
 * 4级:伤害提升至140%攻击力
 */
@Component
public class HunLiJiQu implements SkillEffect {

    @Autowired
    private HpMagicDamage magicDamage;

    @Override
    public EffectType getType() {
        return EffectType.HUN_LI_JI_QU;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        HunLiJiQuParam param = state.getParam(HunLiJiQuParam.class);
        String targetId = param.getTargetId();
        List<Unit> select = TargetSelector.select(owner, targetId, time);
        int totalDamage = 0;
        // 对目标造成伤害
        DamageParam hpDamageParam = new DamageParam(param.getFactor());
        state.setParamOverride(hpDamageParam);
        for (Unit unit : select) {
            magicDamage.execute(state, owner, unit, skillReport, time, skillState, context);
            totalDamage += context.getHpChange(unit);
        }
        state.setParamOverride(null);
        if (totalDamage >= 0) {
            return;
        }
        double damageRate = param.getDamageToHpRate();
        totalDamage *= damageRate;

        // 将伤害平分给队友
        List<Unit> allLive = FilterType.FRIEND.filter(owner, time);
        if (allLive.isEmpty()) {
            return;
        }
        long average = -totalDamage / allLive.size();
        if (average == 0) {
            return;
        }
        average = Math.abs(average);
        for (Unit unit : allLive) {
            context.addValue(unit, AlterType.HP, average);
            skillReport.add(time, unit.getId(), Hp.of(average));
        }
    }
}
