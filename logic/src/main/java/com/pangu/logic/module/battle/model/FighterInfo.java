package com.pangu.logic.module.battle.model;

import com.pangu.logic.module.battle.service.core.Fighter;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.framework.protocol.annotation.Transable;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 战斗单位信息
 */
@Transable
@Getter
public class FighterInfo {

    /**
     * 单位描述信息
     */
    private FighterDescribe describe;

    /**
     * 当前出战中的战斗单元信息
     */
    private List<UnitInfo> current;

    /**
     * 构造方法
     */
    public static FighterInfo valueOf(Fighter fighter) {
        if (fighter == null) {
            return new FighterInfo();
        }
        List<Unit> units = fighter.getCurrent();
        List<UnitInfo> current = new ArrayList<>();
        if (units != null) {
            for (Unit u : units) {
                current.add(UnitInfo.valueOf(u));
            }
        }
        FighterInfo result = new FighterInfo();
        result.current = current;
        result.describe = fighter.getDescribe();
        return result;
    }
}
