package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public class LuHaiBaZhuZSParam {

    /** 区域内击中时加给目标的BUFF标识（专属装备附加） */
    private List<String> zsTargetBuffIds = Collections.emptyList();
    /** 区域内击中时加给友军的BUFF标识（专属装备附加） */
    private String zsFriendBuffId;
    
}
