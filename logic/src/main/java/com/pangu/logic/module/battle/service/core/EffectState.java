package com.pangu.logic.module.battle.service.core;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.resource.EffectSetting;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * 效果状态
 */
@Getter
public class EffectState {

    //  技能效果配置
    private final EffectSetting setting;

    // 攻击范围
    private final int range;

    /**
     * 重写目标选择
     */
    @Setter
    private String targetOverride;

    // 重写参数
    @Setter
    private Object paramOverride;


    @Getter(AccessLevel.PRIVATE)
    @Setter
    private Object addition;

    public EffectState(EffectSetting setting, int range) {
        this.setting = setting;
        this.range = range;
    }

    public String getTarget() {
        if (!StringUtils.isBlank(targetOverride)) {
            return targetOverride;
        }
        return setting.getTarget();
    }

    public String getId() {
        return setting.getId();
    }

    public EffectType getType() {
        return setting.getType();
    }

    @SuppressWarnings("unchecked")
    public <T> T getParam(Class<T> clz) {
        if (paramOverride != null) {
            return (T) paramOverride;
        }
        return (T) setting.getRealParam();
    }

    public int getDelay() {
        return setting.getPlayTime();
    }

    public boolean isReselectTargets() {
        return setting != null && setting.isReselectTargets();
    }

    public boolean isSkipHitCheck() {
        return setting != null && setting.isSkipHitCheck();
    }

    public boolean isDynamicPlayTime() {
        return setting != null && setting.isDynamicPlayTime();
    }

    public <T> T getAddition(Class<T> clz) {
        //noinspection unchecked
        return (T) addition;
    }

    public <T> T getAddition(Class<T> clz, T def) {
        if (addition == null) {
            addition = def;
        }
        return (T) addition;
    }


}
