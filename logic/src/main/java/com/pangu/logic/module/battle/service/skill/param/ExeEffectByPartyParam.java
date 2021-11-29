package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class ExeEffectByPartyParam {
    //<敌我阵营，效果类型>
    private Map<Party, List<String>> partyAndEffect;

    public enum Party {
        ENEMY, FRIEND, ALL
    }
}
