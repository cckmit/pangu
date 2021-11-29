package com.pangu.logic.module.account.service;

import com.pangu.core.anno.ServiceLogic;
import com.pangu.dbaccess.service.EntityService;
import com.pangu.logic.module.account.model.Sex;
import com.pangu.logic.module.player.service.PlayerService;
import com.pangu.logic.server.IdGeneratorHolder;

import java.util.Date;

@ServiceLogic
public class AccountService {

    private final EntityService entityService;

    private final PlayerService playerService;

    private final IdGeneratorHolder idGenerator;

    public AccountService(EntityService entityService,
                          PlayerService playerService,
                          IdGeneratorHolder idGenerator) {
        this.entityService = entityService;
        this.playerService = playerService;
        this.idGenerator = idGenerator;
    }

    public void create(String accountName, String roleName, Sex sex, String channel) {
        String userServerId = Account.toInfo(accountName);
        Account unique = entityService.unique(userServerId, Account.class, "name", accountName);
        if (unique != null) {
            return;
        }
        long id = idGenerator.getNext(userServerId);
        playerService.create(id, roleName, sex);
        Account account = Account.valueOf(id, accountName, channel);
        entityService.create(userServerId, account);
    }

    public boolean login(String account) {
        String userServerId = Account.toInfo(account);
        Account unique = entityService.unique(userServerId, Account.class, "name", account);
        if (unique == null) {
            return false;
        }
        unique.login(new Date(), true, null);
        entityService.updateToDB(userServerId, unique);
        return true;
    }

    public Account loadByName(String uid) {
        String userServerId = Account.toInfo(uid);
        return entityService.unique(userServerId, Account.class, "name", uid);
    }
}
