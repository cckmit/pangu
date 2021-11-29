package com.pangu.logic.module.battle.resource;

import com.pangu.logic.module.battle.model.BattleType;
import com.pangu.framework.resource.anno.Id;
import com.pangu.framework.resource.anno.Resource;
import lombok.Getter;

/**
 * 战斗类型的配置对象
 */
@Resource("battle")
@Getter
public class BattleSetting {

    @Id
    private BattleType id;

    //  最大时间
    private int time;

    // 站位坐标(像素点)
    private int[][] positions;

    // 全体增加怒气间隔
    private int addMpInterval;

    // 全体增加怒气值
    private int addMpValue;

    // 受到伤害每个百分比增加怒气
    private int damagePercent;

    // 每受到伤害百分比增加怒气
    private int damageMp;

    // 击杀一个目标增加怒气
    private int killAddMp;

    // pvp战斗
    private boolean pvp;

    //胜利场次
    private int winTimes;

    // 大招的CD时间
    private int spaceCD;

    // 初始增加怒气
    private int initAddMp;

    // 多场战斗胜负明朗时，是否打完剩余场次
    private boolean lazyResult;

    // 是否暂停
    private boolean pause;
}
