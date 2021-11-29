package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.model.UnitType;
import lombok.Getter;

import java.util.Collections;
import java.util.Set;

@Getter
public class XingChenSheShouZSParam {

    /** 队伍命中提升率 */
    private double hitUpRate;
    /** 伤害率提升所需兵种 */
    private Set<UnitType> harmUpProfessions = Collections.emptySet();
    /** 每比目标多1%命中率的伤害提升比率 */
    private double harmUpRate;
    /** 伤害率提升上限 */
    private double harmUpLimit;

}
