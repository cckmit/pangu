package com.pangu.logic.module.battle.service;


import com.pangu.logic.module.battle.service.core.SkillState;
import io.netty.util.internal.PlatformDependent;
import org.springframework.stereotype.Component;

import java.util.Comparator;

/**
 * 战场固定配置
 */
@Component
public class BattleConstant {

    //  SKILL,NORMAL,SPACE类型技能优先级别排序器
    public static final Comparator<SkillState> SKILL_PRIORITY =
            (o1, o2) -> Integer.compare(o2.getSetting().getPriority(), o1.getSetting().getPriority());
    //  FATTER,INIT类型技能优先级别排序器
    public static final Comparator<SkillState> SPECIAL_SKILL_PRIORITY =
            Comparator.comparingInt(o -> o.getSetting().getPriority());
    // 默认一个单元占格子大小
    public static final int SCOPE = 20;

    // 默认一个单元占格子大小一般
    public static final int SCOPE_HALF = 10;

    // 跟随点的误差半径
    public static final int FOLLOW_SCOPE = 5;

    public static final int VECTOR_DISTANCE = 30;

    // 移动间隔时间
    public static int MoveIntervalMill = 50;

    // 场景最大宽度
    public static int MAX_X = 150;

    // 场景最大高度
    public static int MAX_Y = 140;

    // 怒气上限
    public static long MP_MAX = 1500;

    // 开启战斗超时监测
    public static boolean BATTLE_DEADLINE = !PlatformDependent.isWindows() && !PlatformDependent.isOsx();

    public static volatile long PRE_REPORT = 0;

    public static boolean showReport() {
        long nanoTime = System.nanoTime();
        long sub = nanoTime - PRE_REPORT;
        PRE_REPORT = nanoTime;
        return sub > 10_000_000_000L;
    }
}
