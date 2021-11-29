package com.pangu.logic.module.battle.model;

import com.pangu.framework.protocol.annotation.Transable;
import lombok.Getter;

import java.util.HashMap;

/**
 * 简化单元信息
 */
@Transable
@Getter
public class SimpleUnitInfo {

    /**
     * 单元模型信息
     */
    private ModelInfo model;

    /**
     * 单元血量百分比
     */
    private int hpRate;

    /**
     * 单元能量百分比
     */
    private int mpRate;

    /**
     * 站位顺序
     */
    private int sequence;

    public static SimpleUnitInfo of(UnitBuildInfo unitBuildInfo) {
        SimpleUnitInfo info = new SimpleUnitInfo();
        info.model = unitBuildInfo.getModel();
        final HashMap<UnitValue, Long> values = unitBuildInfo.getValues();
        final Long hp = values.getOrDefault(UnitValue.HP, 0L);
        final Long hpMax = values.getOrDefault(UnitValue.HP_MAX, 0L);
        info.hpRate = (int) Math.ceil(hp * 100D / hpMax);
        final Long mp = values.getOrDefault(UnitValue.MP, 0L);
        final Long mpMax = values.getOrDefault(UnitValue.MP_MAX, 0L);
        info.mpRate = (int) Math.ceil(mp * 10D / mpMax);
        info.sequence = unitBuildInfo.getSequence();
        return info;
    }
}
