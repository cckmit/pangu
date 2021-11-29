package com.pangu.logic.module.player.service;

import com.pangu.core.anno.ServiceLogic;
import com.pangu.framework.resource.Storage;
import com.pangu.framework.resource.anno.Static;
import com.pangu.framework.utils.math.RandomUtils;
import com.pangu.logic.module.player.resource.PlayerNameSetting;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@ServiceLogic
public class PlayerService {
    @Static
    private Storage<Integer, PlayerNameSetting> nameSettingStorage;

    // 角色名对应ID的映射
    private List<String> names = new ArrayList<>(0);

    @PostConstruct
    protected void init() {
        Collection<PlayerNameSetting> settings = nameSettingStorage.getAll();
        List<String> names = new ArrayList<>(settings.size());
        for (PlayerNameSetting setting : settings) {
            names.add(setting.getId());
        }
        this.names = names;
    }

    public String randomName() {
        int size = names.size();
        if (size == 0) {
            return null;
        }
        int random = RandomUtils.nextInt(size);

        return names.get(random);
    }
}
