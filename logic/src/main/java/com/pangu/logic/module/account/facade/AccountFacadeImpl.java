package com.pangu.logic.module.account.facade;

import com.pangu.core.anno.ComponentLogic;
import com.pangu.framework.utils.model.Result;
import com.pangu.logic.module.account.model.LoginInfoVo;
import com.pangu.logic.module.account.model.Sex;
import com.pangu.logic.module.account.service.AccountService;
import com.pangu.logic.utils.ServerIdUtils;
import com.pangu.logic.utils.UTF8Utils;

@ComponentLogic
public class AccountFacadeImpl implements AccountFacade {

    private final AccountService accountService;

    public AccountFacadeImpl(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public Result<Void> create(String account, String name, Sex sex, String channel) {
        String server = ServerIdUtils.extractServerId(account);

//        if (!systemConfig.checkServer(server)) {
//            return Result.ERROR(AccountResult.INVAILD_ACCOUNT_NAME);
//        }
        if (UTF8Utils.checkUTF8MB4(name)) {
            return Result.ERROR(AccountResult.PLAYER_NAME_ILLEGAL);
        }
        accountService.create(account, name, sex, channel);
        return Result.SUCCESS();
    }

    @Override
    public Result<Void> login(String account, Boolean adult, long timestamp, String key) {
        boolean login = accountService.login(account);
        if (!login) {
            return Result.ERROR(AccountResult.ACCOUNT_NOT_FOUND);
        }
        return Result.SUCCESS();
    }

    @Override
    public Result<LoginInfoVo> getLoginInfo(long accountId) {
        return null;
    }

    @Override
    public Result<Boolean> checkAccount(String account) {
        return Result.SUCCESS(false);
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
