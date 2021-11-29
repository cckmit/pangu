package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.service.buff.param.DefaultAddValueParam;
import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import lombok.Getter;

@Getter
public class EffectOnDodgeParam {
    //  修改属性
    private DefaultAddValueParam valModParam;

    //  伤害
    private DamageParam dmgParam;

    //  影响目标，默认为攻击方
    private String target;
}
