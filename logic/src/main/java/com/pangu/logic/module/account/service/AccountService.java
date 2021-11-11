package com.pangu.logic.module.account.service;

import com.pangu.dbaccess.service.EntityService;
import com.pangu.logic.module.account.model.Sex;
import com.pangu.core.anno.ServiceLogic;

@ServiceLogic
public class AccountService {

    private final EntityService entityService;

    public AccountService(EntityService entityService) {
        this.entityService = entityService;
    }

    public void create(String account, String name, Sex sex, String channel) {
        Account acc = Account.valueOf(1, account, channel);
    }
}
