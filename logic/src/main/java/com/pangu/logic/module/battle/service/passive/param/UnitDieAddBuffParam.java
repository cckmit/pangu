package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.service.BattleConstant;
import com.pangu.logic.module.battle.service.skill.param.HpRecoverParam;
import lombok.Getter;

@Getter
public class UnitDieAddBuffParam {
    //添加的BUFF
    private String[] buffs;

    //生命回复
    private HpRecoverParam rcvParam;

    //范围参数
    private int r = BattleConstant.SCOPE;

    //被添加buff的目标
    private String targetId;

    //死亡单位与被动添加者的关系
    private DeadType deadType;

    public enum DeadType {
        OTHER,
        ENEMY,
        FRIEND,
        ENEMY_HERO,
        ENEMY_AROUND,
        SUMMONED,
    }
}
