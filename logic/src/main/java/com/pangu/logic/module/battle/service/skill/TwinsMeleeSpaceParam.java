package com.pangu.logic.module.battle.service.skill;

import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import lombok.Getter;

@Getter
public class TwinsMeleeSpaceParam {
    /**
     * 吸血比例
     */
    private double suckRate;
    /**
     * 触发吸血所需EP
     */
    private int suckEp;

    /**
     * 对被控制目标增伤比例
     */
    private double dmgRate;
    /**
     * 触发增伤所需EP
     */
    private int dmgEp;

    /**
     * 爆伤率增幅
     */
    private double critDmgRate;
    /**
     * 触发爆伤增幅所需EP
     */
    private int critDmgEp;
    /**
     * 最后一击是否必然暴击
     */
    private boolean crit;

    
    private DamageParam dmgParam;
    
    private int[] intervals;
}
