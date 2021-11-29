package com.pangu.logic.module.battle.service.skill.param;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class HpRecoverParam {

    private double factor;

    // 血量恢复公式id [0]暴击ID，[1]普攻id
    private String[] formulaIds;

    // 累计增加次数(有些血量恢复，每次使用后，下次效果增强)
    private int timesLimit;

    // 增加比率
    private double addRate;

    // 额外参数
    private double percent;

    // 额外参数
    private double rate;

    public HpRecoverParam(double factor) {
        this.factor = factor;
    }
}
