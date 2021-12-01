package com.pangu.logic.module.battle.service.buff.param;

import com.pangu.logic.module.battle.service.skill.param.StateAddParam;
import lombok.Getter;


@Getter
public class ListenerAndModifierParam {
    /**
     * 监听内容
     */
    private String filterExp;
    private boolean friend;
    private String listeningTarget;

    /**
     * 修改内容
     */
    private DefaultAddValueParam valModParam;
    private StateAddParam stateAddParam;
    private boolean needStateReport;

    /**
     * 修改策略
     */
    private Strategy strategy = Strategy.ONCE_RECOVERABLE;

    /**
     * 修改策略
     */
    public enum Strategy {
        /**
         * 监听时若满足条件，仅修改一次数据，条件不满足时，回滚修改
         */
        ONCE_RECOVERABLE,
        /**
         * 每次监听若满足条件则修改数据，条件不满足时，不回滚修改
         */
        REPEAT_UNRECOVERABLE,
        
        STATE_ADD_UNRECOVERABLE,
    }
}
