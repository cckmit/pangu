package com.pangu.logic.module.account.facade;

import com.pangu.core.anno.ComponentLogic;
import com.pangu.framework.socket.handler.param.Attachment;
import com.pangu.framework.utils.codec.CryptUtils;
import com.pangu.framework.utils.model.Result;
import com.pangu.framework.utils.time.DateUtils;
import com.pangu.logic.config.LogicConfig;
import com.pangu.logic.config.SystemConfig;
import com.pangu.logic.module.account.model.LoginInfoVo;
import com.pangu.logic.module.account.model.Sex;
import com.pangu.logic.module.account.service.Account;
import com.pangu.logic.module.account.service.AccountService;
import com.pangu.logic.utils.UTF8Utils;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@ComponentLogic
@Slf4j
public class AccountFacadeImpl implements AccountFacade {

    private final AccountService accountService;

    private final LogicConfig logicConfig;

    public AccountFacadeImpl(AccountService accountService, LogicConfig logicConfig) {
        this.accountService = accountService;
        this.logicConfig = logicConfig;
    }

    @Override
    public Result<Void> create(String account, String name, Sex sex, String channel) {
        if (UTF8Utils.checkUTF8MB4(name)) {
            return Result.ERROR(AccountResult.PLAYER_NAME_ILLEGAL);
        }
        accountService.create(account, name, sex, channel);
        return Result.SUCCESS();
    }

    @Override
    public Result<Void> login(String uid, Boolean adult, long timestamp, String key, Attachment attachment) {
        if (loginKeyNotValid(uid, timestamp, key)) {
            return Result.ERROR(AccountResult.LOGIN_KEY_ILLEGAL);
        }
        Account account = accountService.loadByName(uid);
        if (account == null) {
            return Result.ERROR(AccountResult.ACCOUNT_NOT_FOUND);
        }

        boolean login = accountService.login(uid);
        if (!login) {
            return Result.ERROR(AccountResult.ACCOUNT_NOT_FOUND);
        }
        attachment.identity(account.getId());
        return Result.SUCCESS();
    }

    private boolean loginKeyNotValid(String accountName, long timestamp, String key) {
        SystemConfig system = logicConfig.getSystem();
        int checkLoginKeyValid = system.getCheckLoginKeyValid();
        if (checkLoginKeyValid > 0) {
            long current = System.currentTimeMillis() / 1000;
            if (timestamp > Integer.MAX_VALUE) {
                timestamp = timestamp / 1000;
            }
            if (current - timestamp > checkLoginKeyValid) {
                log.error("帐号[{}]登录验证MD5时间[{}]超时", accountName, DateUtils.format2Ymdhms(new Date(timestamp * 1000)));
                return true;
            }
        }
        String origin = Account.toOrigin(accountName);
        String baseString = origin + timestamp + system.getLoginKey();
        try {
            String md5 = CryptUtils.md5(baseString).toLowerCase();
            if (md5.equalsIgnoreCase(key)) {
                return false;
            } else {
                log.error("signError:account:{},timestamp:{},key:{}", origin, timestamp, key);
                log.error("server sign:{}", md5);
            }
        } catch (Exception e) {
            log.error("检查登录密匙时发生异常", e);
        }
        return true;
    }

    @Override
    public Result<LoginInfoVo> getLoginInfo(long accountId) {
        return null;
    }

    @Override
    public Result<Boolean> checkAccount(String uid) {
        Account account = accountService.loadByName(uid);
        return Result.SUCCESS(account != null);
    }

    @Override
    public Result<String> randomName(Sex sex) {
        return null;
    }

    @Override
    public Result<Void> loginComplete(long accountId) {
        return null;
    }

    @Override
    public Result<Integer> updateChannel(long accountId, String channel) {
        return null;
    }
}
