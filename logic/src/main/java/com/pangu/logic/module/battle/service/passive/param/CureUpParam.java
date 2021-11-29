package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

import java.util.Set;

@Getter
public class CureUpParam {
    //  监听的skillId前缀
    private Set<String> skillIdPrefixes;
    //  监听的buffId前缀
    private Set<String> buffIdPrefixes;

    //  其他条件
    private String subCond;

    //  治疗量加成
    private double cureUpRate;
}
