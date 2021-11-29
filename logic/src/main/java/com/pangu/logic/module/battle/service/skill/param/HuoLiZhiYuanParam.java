package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class HuoLiZhiYuanParam {

    /** 半径范围 */
    private int radius;
    /** 伤害系数 */
    private double factor;
    /** 区域内加给自己的BUF标识 */
    private String ownerBuffId;
    /** 区域内加给目标的BUFF标识 */
    private List<String> targetBuffIds = Collections.emptyList();
    /** 区域内击中时加给目标的BUFF标识（专属装备附加） */
    private List<String> zsTargetBuffIds = Collections.emptyList();
    /** 区域内击中时加给友军的BUFF标识（专属装备附加） */
    private String zsFriendBuffId;
    
}
