package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.UnitType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.QiYuanDeNaHanParam;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import com.pangu.logic.module.battle.service.skill.effect.StateAddEffect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * 启源的呐喊
 * 尼德霍格蓄力生成龙珠，龙珠飞向敌人最密集的区域造成爆炸
 * 对力量英雄：造成巨量伤害；
 * 对智力英雄：根据其自身能量造成伤害，并移除所有能量；
 * 对敏捷英雄：造成伤害，并魅惑5秒
 */
@Component
public class QiYuanDeNaHan implements AttackPassive {
    // 该被动实际为主动效果的一部分，逻辑上不需要传被动战报
    @Autowired
    private StateAddEffect stateAddEffect;

    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (!skillState.getTag().equals("qi_yuan_de_na_han")) {
            return;
        }
        final QiYuanDeNaHanParam param = passiveState.getParam(QiYuanDeNaHanParam.class);
        final Collection<UnitType> professions = target.getProfession();
        if (professions.contains(UnitType.AGILITY)) {
            final EffectState effectState = new EffectState(null, 0);
            effectState.setParamOverride(param.getStateAddParam());
            stateAddEffect.execute(effectState, owner, target, skillReport, time, skillState, context);
        }
        if (professions.contains(UnitType.INTELLECT)) {
            final long dmgChange = (long) (damage * (target.getValue(UnitValue.MP) / 100 * param.getDmgUpRateForIntellectPer100Mp()));
            PassiveUtils.hpUpdate(context, skillReport, target, dmgChange, time);
            PassiveUtils.mpUpdate(context, skillReport, owner, target, -target.getValue(UnitValue.MP), time, passiveState);
        }
        if (professions.contains(UnitType.STRENGTH)) {
            final long dmgChange = (long) (damage * param.getDmgUpRateForStrength());
            PassiveUtils.hpUpdate(context, skillReport, target, dmgChange, time);
        }
    }

    @Override
    public PassiveType getType() {
        return PassiveType.QI_YUAN_DE_NA_HAN;
    }
}
