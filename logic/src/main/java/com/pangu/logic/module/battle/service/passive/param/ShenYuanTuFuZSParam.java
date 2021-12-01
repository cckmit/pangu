package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Getter
public class ShenYuanTuFuZSParam {

    
    private Set<String> skillTags = Collections.emptySet();
    
    private List<String> buffIds = Collections.emptyList();
    
    private double harmUpHpPct;
    
    private double harmUpRate;
    
    private double harmUpCd;

}
