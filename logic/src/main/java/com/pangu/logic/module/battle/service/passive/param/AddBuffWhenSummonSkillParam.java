package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.model.EffectType;
import lombok.Getter;

import java.util.Collections;
import java.util.Set;

@Getter
public class AddBuffWhenSummonSkillParam {

    private String[] buffs;

    private int overlayLimit = 1;

    private Set<EffectType> effectTypes = Collections.singleton(EffectType.SUMMON);
}
