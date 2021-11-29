package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.service.skill.param.HpRecoverParam;
import lombok.Getter;

@Getter
public class CureOnOwnerHpDownParam {
    /**
     * 添加的BUFF
     */
    private String buff;

    /**
     * 治疗的目标
     */
    private String cureTarget;

    /**
     * 治疗参数
     */
    private HpRecoverParam recoverParam;

    /**
     * 触发血线
     */
    private double triggerHpPct;
}
