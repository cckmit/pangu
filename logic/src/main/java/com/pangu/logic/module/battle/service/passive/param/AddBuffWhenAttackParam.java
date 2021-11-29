package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.model.SkillType;
import lombok.Getter;

@Getter
public class AddBuffWhenAttackParam {
    //初始概率
    private double baseRate;
    //每次叠加概率
    private double rateIncrease;
    //BuffId
    private String[] buffs;
    //添加BUFF的目标
    private String targetId;
    //满足添加条件的攻击类型
    private SkillType[] types;
    //最大成功添加BUFF次数
    private int maxTrigger;
}
