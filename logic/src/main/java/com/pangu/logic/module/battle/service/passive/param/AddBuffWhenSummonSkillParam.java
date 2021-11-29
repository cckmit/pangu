package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.model.EffectType;
import lombok.Getter;

import java.util.Collections;
import java.util.Set;

@Getter
public class AddBuffWhenSummonSkillParam {
    /* 添加的BUFF标识 */
    private String[] buffs;
    /* 每个召唤物叠加次数上限 */
    private int overlayLimit = 1;
    /* 关注的召唤技能类型*/
    private Set<EffectType> effectTypes = Collections.singleton(EffectType.SUMMON);
}
