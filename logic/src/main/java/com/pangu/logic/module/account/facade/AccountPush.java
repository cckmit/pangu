package com.pangu.logic.module.account.facade;

import com.pangu.framework.socket.anno.Identity;
import com.pangu.framework.socket.anno.SocketCommand;
import com.pangu.framework.socket.anno.SocketModule;
import com.pangu.framework.socket.anno.SocketPush;

import static com.pangu.logic.module.account.facade.AccountModule.*;

/**
 * 账号服务推送接口
 *
 * @author frank
 */
@SocketPush
@SocketModule(MODULE)
public interface AccountPush {
    /**
     * 用户强制退出(发生在同一个账号有重复登录时)
     */
    @SocketCommand(PUSH_ENFORCE_LOGOUT)
    void enforceLogout(@Identity long id);

    /**
     * 用于通知客户端防沉迷状态是否被激活.
     *
     * @param enable 激活返回true,否则返回false;
     */
    @SocketCommand(PUSH_FATIGUE_STATE)
    void fatigueState(boolean enable);

    /**
     * 推送状态变更事件
     *
     * @param state 最新状态
     */
    @SocketCommand(PUSH_CHANGE_STATE)
    void changeState(@Identity long id, int state);

    /**
     * 踢下线通知
     */
    @SocketCommand(KICK_OUT)
    void kickOut(@Identity long id);
}
