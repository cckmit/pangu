package com.pangu.logic.module.battle.service;

import com.pangu.logic.module.battle.resource.*;
import com.pangu.framework.resource.Storage;
import com.pangu.framework.resource.anno.Static;
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
    @Static
    private Storage<String, EnemyUnitSetting2> enemyUnitSetting2Storage;
    @Static
    private Storage<String, EnemyUnitSetting3> enemyUnitSetting3Storage;
    @Static
    private Storage<String, EnemyUnitSetting4> enemyUnitSetting4Storage;
    @Static
    private Storage<String, EnemyUnitSetting5> enemyUnitSetting5Storage;


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
        init2();
        init3();
        init4();
        init5();
        enemyUnitSettingStorage.addObserver((o, arg) -> init1());
        enemyUnitSetting2Storage.addObserver((o, arg) -> init2());
        enemyUnitSetting3Storage.addObserver((o, arg) -> init3());
        enemyUnitSetting4Storage.addObserver((o, arg) -> init4());
        enemyUnitSetting5Storage.addObserver((o, arg) -> init5());
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

    private void init2() {
        for (EnemyUnitSetting2 setting : enemyUnitSetting2Storage.getAll()) {
            cache.put(setting.getId(), setting.toEnemyUnitSetting());
        }
    }

    private void init3() {
        for (EnemyUnitSetting3 setting : enemyUnitSetting3Storage.getAll()) {
            cache.put(setting.getId(), setting.toEnemyUnitSetting());
        }
    }

    private void init4() {
        for (EnemyUnitSetting4 setting : enemyUnitSetting4Storage.getAll()) {
            cache.put(setting.getId(), setting.toEnemyUnitSetting());
        }
    }

    private void init5() {
        for (EnemyUnitSetting5 setting : enemyUnitSetting5Storage.getAll()) {
            cache.put(setting.getId(), setting.toEnemyUnitSetting());
        }
    }
}
