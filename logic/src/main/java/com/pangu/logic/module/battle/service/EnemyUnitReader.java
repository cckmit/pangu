package com.pangu.logic.module.battle.service;

import com.pangu.framework.resource.Storage;
import com.pangu.framework.resource.anno.Static;
import com.pangu.logic.module.battle.resource.EnemyUnitSetting;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class EnemyUnitReader {
    @Static
    private Storage<String, EnemyUnitSetting> enemyUnitSettingStorage;


    private ConcurrentHashMap<String, EnemyUnitSetting> cache = new ConcurrentHashMap<>(10000);

    public Collection<EnemyUnitSetting> getAll() {
        return cache.values();
    }

    public EnemyUnitSetting get(String id, boolean flag) {
        final EnemyUnitSetting setting = cache.get(id);
        if (setting == null && flag) {
            FormattingTuple message = MessageFormatter.format("怪物表标识为[{}]的静态资源不存在", id);
            log.error(message.getMessage());
            throw new IllegalStateException(message.getMessage());
        }
        return setting;
    }

    public void start() {
        init1();
        enemyUnitSettingStorage.addObserver((o, arg) -> init1());
    }

    public void stop() {

    }

    public boolean isRunning() {
        return false;
    }

    private void init1() {
        for (EnemyUnitSetting setting : enemyUnitSettingStorage.getAll()) {
            cache.put(setting.getId(), setting);
        }
    }
}
