package com.pangu.logic.module.account.service;

import com.pangu.core.anno.ServiceLogic;
import com.pangu.dbaccess.service.EntityService;
import com.pangu.logic.module.account.model.Sex;
import com.pangu.logic.server.IdGeneratorHolder;

@ServiceLogic
public class AccountService {

    private final EntityService entityService;

    private final IdGeneratorHolder idGenerator;

    public AccountService(EntityService entityService, IdGeneratorHolder idGenerator) {
        this.entityService = entityService;
        this.idGenerator = idGenerator;
    }

    public void create(String accountName, String roleName, Sex sex, String channel) {
        String userServerId = Account.toInfo(accountName);
        Account unique = entityService.unique(userServerId, Account.class, "name", accountName);
        if (unique != null) {
            return;
        }
        long id = idGenerator.getNext(userServerId);
        Account account = Account.valueOf(id, accountName, channel);
        entityService.create(userServerId, account);
    }

    public boolean login(String account) {
        String userServerId = Account.toInfo(account);
        Account unique = entityService.unique(userServerId, Account.class, "name", account);
        return unique != null;
    }
}
