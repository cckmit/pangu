package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.model.CalType;
import com.pangu.logic.module.battle.model.UnitRate;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.service.passive.Phase;
import lombok.Getter;

import java.util.Collections;
import java.util.Map;

@Getter
public class SkillAttributeAlterParam {

    /** 触发阶段 */
    private Phase phase;
    /** 变更属性是否作用于自己 */
    private boolean alterOwner;
    /** 变更条件，为空表示 */
    private String condition;
    /** 属性计算类型 */
    private CalType calType;
    /** 变更的属性值 */
    private Map<UnitValue, String> values = Collections.emptyMap();
    /** 变更的属性比率 */
    private Map<UnitRate, String> rates = Collections.emptyMap();

}
