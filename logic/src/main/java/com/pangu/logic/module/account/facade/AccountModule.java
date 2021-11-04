package com.pangu.logic.module.account.facade;

import com.pangu.framework.socket.anno.SocketDefine;

/**
 * 玩家角色模块指令定义
 *
 * @author frank
 */
@SocketDefine
public interface AccountModule {

    /**
     * 当前的模块标识:12
     */
    short MODULE = 12;

    /**
     * 频道模块的通信模块定义:[12]
     */
    byte[] MODULES = {MODULE};

    // 指令值定义部分

    /**
     * 创建账号与角色
     */
    int COMMAND_CREATE = 1;

    /**
     * 账号登录
     */
    int COMMAND_LOGIN = 2;

    /**
     * 重登录(断线重连的登录方法)
     */
    int COMMAND_RELOGIN = 3;

    /**
     * 检查账号是否存在
     */
    int COMMAND_CHECK_ACCOUNT = 4;

    /**
     * 获取账号信息
     */
    int COMMAND_LOGIN_INFO = 7;

    /**
     * 登录完成
     */
    int COMMAND_LOGIN_COMPLETE = 9;

    /**
     * 得到随机名
     */
    int COMMAND_RANDOM_NAME = 10;

    /**
     * 更新渠道信息
     */
    int COMMAND_UPDATE_CHANNEL = 11;

    // 推送部分的指令定义

    /**
     * 用户强制退出(发生在同一个账号有重复登录时)
     */
    int PUSH_ENFORCE_LOGOUT = -1;

    /**
     * 用于通知客户端防沉迷状态是否被激活.
     *
     * @param enable 激活返回true,否则返回false;
     */
    int PUSH_FATIGUE_STATE = -2;

    /**
     * 推送状态变更事件
     */
    int PUSH_CHANGE_STATE = -3;

    /**
     * 踢下线通知
     */
    int KICK_OUT = -4;
}
