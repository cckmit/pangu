package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class HuoLiZhiYuanParam {


    private int radius;

    private double factor;

    private String ownerBuffId;

    private List<String> targetBuffIds = Collections.emptyList();

    private List<String> zsTargetBuffIds = Collections.emptyList();

    private String zsFriendBuffId;
    
}
