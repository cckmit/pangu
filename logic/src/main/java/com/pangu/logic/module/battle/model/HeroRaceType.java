package com.pangu.logic.module.battle.model;

import com.pangu.framework.protocol.annotation.Transable;

/**
 * 英雄阵营(种族)类型
 */
@Transable
public enum HeroRaceType {
    /**
     * 银白帝国
     */
    YBDG(UnitRate.YBDG_DEEPEN),
    /**
     * 荒野之地
     */
    HYZD(UnitRate.HYZD_DEEPEN),
    /**
     * 自然回响
     */
    ZRHX(UnitRate.ZRHX_DEEPEN),
    /**
     * 坠星之城
     */
    ZXZC(UnitRate.ZXZC_DEEPEN),
    /**
     * 深渊魔井
     */
    SYMJ(UnitRate.SYMJ_DEEPEN),
    /**
     * 永恒神域
     */
    YHSY(UnitRate.YHSY_DEEPEN),
    /**
     * 虚空英雄
     */
    XKYX,
    ;

    //克制属性类型
    UnitRate deepen;

    HeroRaceType() {
    }

    HeroRaceType(UnitRate deepen) {
        this.deepen = deepen;
    }

    public UnitRate getDeepen() {
        return deepen;
    }
}
