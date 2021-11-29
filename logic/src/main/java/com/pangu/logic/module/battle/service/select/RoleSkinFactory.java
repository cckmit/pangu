package com.pangu.logic.module.battle.service.select;

import com.pangu.logic.module.battle.service.BattleConstant;
import com.pangu.logic.module.battle.resource.RoleSkin;
import com.pangu.framework.resource.Storage;
import com.pangu.framework.resource.anno.Static;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class RoleSkinFactory {

    @Static
    private Storage<Integer, RoleSkin> skinStorage;

    private static RoleSkinFactory INSTANCE;

    @PostConstruct
    void init() {
        INSTANCE = this;
    }

    public static int getScope(int modelId) {
        RoleSkin roleSkin = INSTANCE.skinStorage.get(modelId, false);
        if (roleSkin == null) {
            return BattleConstant.SCOPE;
        }
        return roleSkin.getScope();
    }
}
