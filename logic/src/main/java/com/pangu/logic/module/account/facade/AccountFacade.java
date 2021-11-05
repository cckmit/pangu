package com.pangu.logic.module.account.facade;

import com.pangu.framework.socket.anno.Identity;
import com.pangu.framework.socket.anno.InBody;
import com.pangu.framework.socket.anno.SocketCommand;
import com.pangu.framework.socket.anno.SocketModule;
import com.pangu.framework.socket.handler.Session;
import com.pangu.framework.utils.model.Result;
import com.pangu.logic.module.account.model.LoginInfoVo;
import com.pangu.logic.module.account.model.Sex;

import static com.pangu.logic.module.account.facade.AccountModule.*;

@SocketModule(MODULE)
public interface AccountFacade {

    /**
     * 创建账号与角色
     *
     * @param account    帐号名
     * @param name       角色名称
     * @param sex        角色类型
     * @param channel    帐号来源渠道标识
     * @return
     */
    @SocketCommand(COMMAND_CREATE)
    Result<Void> create(@InBody("account") String account,
                        @InBody(value = "name", required = false) String name,
                        @InBody("sex") Sex sex,
                        @InBody(value = "channel", required = false) String channel);

    /**
     * 账号登录
     *
     * @param account   账号名(包括服标识)
     * @param timestamp 时间戳
     * @param adult     是否成年
     * @param key       加密串
     * @return {@link Result} { code:状态码{@link AccountResult} content:用户登录信息{@link LoginInfoVo}
     */
    @SocketCommand(value = COMMAND_LOGIN)
    Result<Void> login(@InBody("account") String account,
                       @InBody(value = "adult", required = false) Boolean adult,
                       @InBody("timestamp") long timestamp,
                       @InBody("key") String key);

    /**
     * 获取账号信息
     *
     * @param accountId
     * @return
     */
    @SocketCommand(value = COMMAND_LOGIN_INFO)
    Result<LoginInfoVo> getLoginInfo(@Identity long accountId);

    /**
     * 检查账号和对应角色是否存在
     *
     * @param account 账号名(包括服标识)
     * @return
     */
    @SocketCommand(COMMAND_CHECK_ACCOUNT)
    Result<Boolean> checkAccount(@InBody("account") String account);

    /**
     * 得到随机名
     */
    @SocketCommand(value = COMMAND_RANDOM_NAME)
    Result<String> randomName(@InBody Sex sex);

    /**
     * 登录完成
     *
     * @return 状态码{@link AccountResult}
     */
    @SocketCommand(COMMAND_LOGIN_COMPLETE)
    Result<Void> loginComplete(@Identity long accountId);

    /**
     * 更新渠道
     *
     * @param accountId
     * @param channel
     * @return
     */
    @SocketCommand(COMMAND_UPDATE_CHANNEL)
    Result<Integer> updateChannel(@Identity long accountId, @InBody String channel);
}
