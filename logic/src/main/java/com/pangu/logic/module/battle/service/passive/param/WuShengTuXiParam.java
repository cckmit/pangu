package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.service.BattleConstant;
import lombok.Getter;

@Getter
public class WuShengTuXiParam {

    // 技能ID
    private String skillId;

    // 技能ID
    private String buff;

    // 瞬移后距离目标的距离
    private int dist = BattleConstant.SCOPE;
}
