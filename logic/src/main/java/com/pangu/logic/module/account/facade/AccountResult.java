package com.pangu.logic.module.account.facade;

import com.pangu.framework.protocol.annotation.Constant;
import com.pangu.framework.utils.model.ResultCode;

/**
 * 账号服务状态码声明
 *
 * @author author
 */
@Constant
public interface AccountResult extends ResultCode {

    /**
     * 账号已经存在
     */
    int ACCOUNT_ALREADY_EXISTS = -12001;

    /**
     * 角色已经存在
     */
    int PLAYER_ALREADY_EXISTS = -12002;

    /**
     * 角色名非法
     */
    int PLAYER_NAME_ILLEGAL = -12003;

    /**
     * 登录密匙非法
     */
    int LOGIN_KEY_ILLEGAL = -12004;

    /**
     * 账号不存在
     */
    int ACCOUNT_NOT_FOUND = -12005;

    /**
     * 重登录失败
     */
    int RELOGIN_FAIL = -12006;

    /**
     * 非法的账号名
     */
    int INVAILD_ACCOUNT_NAME = -12007;

    /**
     * 初始化奖励内容错误
     */
    int INIT_REWARD_ERROR = -12008;

    /**
     * 账号已经被封
     */
    int ACCOUNT_IS_BLOCK = -12009;

    /**
     * 停止注册
     */
    int UNREGISTABLE = -12010;

    /**
     * 随机名不存在
     */
    int RANDOM_NAME_NOT_EXISTS = -12011;

}
