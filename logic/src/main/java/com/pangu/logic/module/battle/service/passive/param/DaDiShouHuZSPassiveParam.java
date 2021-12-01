package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public class DaDiShouHuZSPassiveParam {

    
    private String skillTag;
    
    private List<String> buffIds = Collections.emptyList();
    
    private int sneerMpRecover;
    
    private String sneerBuffId;

}
