package com.pangu.logic.module.battle.model;

import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.sort.*;
import com.pangu.framework.protocol.annotation.Transable;

import java.util.List;

/**
 * 排序方式
 * author weihongwei
 * date 2018/3/27
 */
@Transable
public enum SortType {

    /**
     * 战力从高到低
     */
    FIGHT_HIGH(new FightHight()),

    /**
     * 按Y轴方向距离从近到远
     */
    Y_DISTANCE(new YDistance()),

    /**
     * 按序号排列(可用于前排到后排的排列)
     */
    SEQUENCE(new Sequence()),

    /**
     * 累计伤害从高到低
     */
    DMG_HIGH(new DmgHigh()),

    
    DMG_LOW(new DmgLow()),

    /**
     * 从远到近
     */
    FAR_TO_NEAR(new FarToNear()),


    /**
     * 随机排序
     */
    RANDOM(new Random()),


    /**
     * 攻击力排序
     */
    ATK_HIGH(new ATK_High()),

    
    DEF_P_LOW(new DEF_P_LOW()),

    /**
     * 按照距离
     */
    DISTANCE(new Distance()),

    /**
     * 血量(从低到高)
     */
    HP_LOW(new HP_Low()),

    
    HP_PCT_LOW(new HpPctLow()),

    /**
     * 血量从高到低
     */
    HP_HIGH(new HP_High()),

    
    MP_LOW(new MP_Low()),

    
    MP_HIGH(new MP_High()),

    ;

    private SortProcessor sortProcessor;

    SortType(SortProcessor sortProcessor) {
        this.sortProcessor = sortProcessor;
    }

    public List<Unit> sort(Unit unit, List<Unit> units, SelectSetting selectSetting) {
        SelectType selectType = selectSetting.getSelectType();
        Point position;
        if (selectType == SelectType.TARGET_CIRCLE) {
            position = unit.getTarget().getPoint();
        } else {
            position = unit.getPoint();
        }
        return sortProcessor.sort(position, units);
    }
}
