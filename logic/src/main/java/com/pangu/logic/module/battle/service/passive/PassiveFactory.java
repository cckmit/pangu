package com.pangu.logic.module.battle.service.passive;

import com.pangu.logic.module.battle.facade.BattleResult;
import com.pangu.logic.module.battle.resource.PassiveSetting;
import com.pangu.framework.resource.Storage;
import com.pangu.framework.resource.anno.Static;
import com.pangu.framework.utils.ManagedException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collection;

/**
 * 被动效果工厂
 */
@Component
@Slf4j
public class PassiveFactory {

    @Autowired
    private Collection<Passive> passiveList;

    @Static
    private Storage<String, PassiveSetting> configs;

    //  技能效果实例
    private final Passive[] passives = new Passive[PassiveType.values().length];

    private static PassiveFactory INSTANCE;

    //  初始化方法
    @PostConstruct
    protected void init() {
        for (Passive passive : passiveList) {
            int ordinal = passive.getType().ordinal();
            if (passives[ordinal] != null) {
                throw new IllegalStateException("被动效果类型实例重复" + passive.getType());
            }
            passives[ordinal] = passive;
        }
        PassiveFactory.INSTANCE = this;
        passiveList.clear();
        passiveList = null;
    }

    public static Passive getPassive(String id) {
        return INSTANCE.getPassive0(id);
    }

    Passive getPassive0(String id) {
        PassiveSetting config = configs.get(id, false);
        if (config == null) {
            FormattingTuple message = MessageFormatter.format("标识为[{}]被动效果配置不存在", id);
            log.error(message.getMessage());
            throw new ManagedException(BattleResult.CONFIG_ERROR, message.getMessage());
        }
        Passive result = passives[config.getType().ordinal()];
        if (result == null) {
            FormattingTuple message = MessageFormatter.format("被动效果类型[{}]的实例不存在", config.getType());
            log.error(message.getMessage());
            throw new ManagedException(BattleResult.CONFIG_ERROR, message.getMessage());
        }
        return result;
    }

    public static <T> T getPassive(PassiveType type) {
        //noinspection unchecked
        return (T) INSTANCE.getPassive0(type);
    }

    Passive getPassive0(PassiveType type) {
        Passive result = passives[type.ordinal()];
        if (result == null) {
            FormattingTuple message = MessageFormatter.format("被动效果类型[{}]的实例不存在", type);
            log.error(message.getMessage());
            throw new ManagedException(BattleResult.CONFIG_ERROR, message.getMessage());
        }
        return result;
    }

    public static PassiveState initState(String id, int time) {
        return INSTANCE.initState0(id, time);
    }

    PassiveState initState0(String id, int time) {
        PassiveSetting config = configs.get(id, false);
        if (config == null) {
            FormattingTuple message = MessageFormatter.format("标识为[{}]被动效果配置不存在", id);
            log.error(message.getMessage());
            throw new ManagedException(BattleResult.CONFIG_ERROR, message.getMessage());
        }
        return new PassiveState(config, time);
    }
}
