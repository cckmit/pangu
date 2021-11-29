package com.pangu.logic.module.battle.service.action;

/**
 * 行动抽象接口
 */
public interface Action extends Comparable<Action> {

    /**
     * 行动执行时间
     *
     * @return
     */
    int getTime();

    /**
     * 执行
     */
    void execute();

    @Override
    default int compareTo(Action o) {
        return Integer.compare(getTime(), o.getTime());
    }
}
