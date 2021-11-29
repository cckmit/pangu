package com.pangu.logic.module.battle.service.skill.param;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.CalType;
import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.service.BattleConstant;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class SummonSkillParam {

    /**
     * 召唤几只怪(可选)
     */
    private int summonAmount = 1;

    /**
     * 移除之前召唤的(可选)
     */
    private boolean removePreSummon;

    /**
     * 单元ID(必填)
     */
    private String baseId;

    /** 0:正常召唤物     1:源石神像      2:主神试炼*/
    private int unitType;
    /**
     * 属性比率(可选 和计算方式2选一)
     */
    private double rate;

    //召唤物属性 计算方式
    private CalType calType;

    //召唤物属性 属性值
    private Map<AlterType, String> alters;

    //召唤物落点位置(可选)
    private Point point;

    //是否根据队伍方向进行翻转
    private boolean reverse;

    //是否标识为召唤物(可选)
    private boolean summon = true;
    //是否加入可选队列
    private boolean joinFighter = true;

    //召唤物的属性以哪个单位为基准(可选)
    private String targetId = "SELF";

    //召唤物的默认位置参数(可选)
    //召唤位置距离召唤者的距离
    private int dist = BattleConstant.SCOPE_HALF;
    //角色朝向与召唤位置的夹角度数
    private int angle = 120;
    //召唤物从哪个单位附近出生
    private String momSelectId = "SELF";
}
