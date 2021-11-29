package com.pangu.logic.module.battle.service.skill.ctx;

import com.pangu.logic.module.battle.model.UnitRate;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import com.pangu.logic.module.battle.service.core.Unit;
import lombok.Getter;

import java.util.Map;

/**
 * 物理攻击以及法术攻击公式
 */
@Getter
public class AttackCtx {
    private final int time;
    private final WrapperUnit owner;
    private final WrapperUnit target;

    // 伤害比率
    private final double factor;

    // 根据技能类型(普攻、大招，技能)伤害比率不同
    private final double skillAttackAddRate;

    public AttackCtx(int time, Unit owner, Unit target, DamageParam damageParam, double skillAttackAddRate) {
        this.time = time;
        this.owner = new WrapperUnit(owner);
        this.target = new WrapperUnit(target);
        this.factor = damageParam.getFactor();
        this.skillAttackAddRate = skillAttackAddRate;
        Map<UnitRate, Double> ownerAdd = damageParam.getOwnerAddRate();
        this.owner.setRateAdd(ownerAdd);
        Map<UnitValue, Double> targetValueChangeRate = damageParam.getTargetValueChangeRate();
        this.target.setValueRate(targetValueChangeRate);
    }
}
