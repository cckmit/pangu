package com.pangu.logic.module.battle.service.passive;

/**
 * 被动优先级定义
 */
public interface Priority {

    // 优先级最高，最先执行
    int HIGHEST = 0;

    // 常规被动
    int NORMAL = 10;

    // 最后执行
    int LOWEST = 20;

    // 常用于死亡状态判断
    int END = 30;
}
