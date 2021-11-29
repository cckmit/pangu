package com.pangu.logic.module.battle.service.skill.param;

import com.pangu.logic.module.battle.model.UnitRate;
import com.pangu.logic.module.battle.model.UnitValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DamageParam {

    // 攻击比率参数
    private double factor;

    // 暴击触发公式
    private String critExp;

    // 个人提升
    private Map<UnitRate, Double> ownerAddRate;

    // 直接修改目标的属性值到某个百分比
    private Map<UnitValue, Double> targetValueChangeRate;

    public DamageParam(double factor) {
        this.factor = factor;
    }

    public DamageParam copy(double factor) {
        final DamageParam damageParam = new DamageParam();
        damageParam.factor = factor;
        damageParam.critExp = this.critExp;
        damageParam.ownerAddRate = this.ownerAddRate;
        damageParam.targetValueChangeRate = this.targetValueChangeRate;
        return damageParam;
    }

    public DamageParam copy() {
        final DamageParam damageParam = new DamageParam();
        damageParam.factor = this.factor;
        damageParam.critExp = this.critExp;
        damageParam.ownerAddRate = this.ownerAddRate;
        damageParam.targetValueChangeRate = this.targetValueChangeRate;
        return damageParam;
    }
}
