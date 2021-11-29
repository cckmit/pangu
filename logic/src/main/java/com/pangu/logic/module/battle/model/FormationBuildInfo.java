package com.pangu.logic.module.battle.model;

import com.pangu.framework.protocol.annotation.Transable;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 队伍构建信息
 */
@Transable
@Data
public class FormationBuildInfo {
    /**
     * 所属玩家ID
     */
    private long owner;
    /**
     * 当前英雄信息
     */
    private List<UnitBuildInfo> unitBuildInfos;

    /**
     * 当前羁绊
     */
    private Map<UnitType, HeroJobType> select;

    /**
     * 装备的源石神像（可选 针对招募英雄配置源石神像技能使用
     * 注意！！！其他情况下添加会可能会导致出现多个源石神像技能）
     */
    private Integer originalStatue;


    // 信息描述，会记录进入战报
    private FighterDescribe describe;

    public int averageLevel() {
        int totalLevel = 0;
        for (UnitBuildInfo buildInfo : unitBuildInfos) {
            totalLevel += buildInfo.getModel().getLevel();
        }
        return totalLevel / unitBuildInfos.size();
    }

    public int averageFight() {
        int totalFight = 0;
        for (UnitBuildInfo buildInfo : unitBuildInfos) {
            totalFight += buildInfo.getModel().getFight();
        }
        return totalFight / unitBuildInfos.size();
    }

    public FormationBuildInfo deepCopy() {
        FormationBuildInfo info = new FormationBuildInfo();
        info.owner = owner;
        if (unitBuildInfos != null) {
            ArrayList<UnitBuildInfo> list = new ArrayList<>(unitBuildInfos.size());
            for (UnitBuildInfo unitBuildInfo : unitBuildInfos) {
                list.add(unitBuildInfo.copy());
            }
            info.unitBuildInfos = list;
        }
        if (select != null) {
            info.select = new HashMap<>(select);
        }
        info.originalStatue = originalStatue;
        return info;
    }

    public FormationBuildInfo copy() {
        FormationBuildInfo info = new FormationBuildInfo();
        info.owner = owner;
        if (unitBuildInfos != null) {
            info.unitBuildInfos = new ArrayList<>(unitBuildInfos);
        }
        if (select != null) {
            info.select = new HashMap<>(select);
        }
        info.originalStatue = originalStatue;
        return info;
    }

    public static FormationBuildInfo of(long owner, List<UnitBuildInfo> unitBuildInfos, Map<UnitType, HeroJobType> select, FighterDescribe describe) {
        return of(owner, unitBuildInfos, select, describe, null);
    }

    public static FormationBuildInfo of(long owner, List<UnitBuildInfo> unitBuildInfos, Map<UnitType, HeroJobType> select, FighterDescribe describe, Integer originalStatue) {
        FormationBuildInfo buildInfo = new FormationBuildInfo();
        buildInfo.owner = owner;
        buildInfo.unitBuildInfos = unitBuildInfos;
        buildInfo.select = select;
        buildInfo.describe = describe;
        buildInfo.originalStatue = originalStatue;
        return buildInfo;
    }
}
