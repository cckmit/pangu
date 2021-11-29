package com.pangu.logic.module.battle.service.passive;


import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.resource.PassiveSetting;
import com.pangu.logic.module.battle.service.core.Unit;
import lombok.Getter;
import lombok.Setter;

/**
 * 被动状态
 */
@Getter
@Setter
public class PassiveState {

    // 被动配置
    private final PassiveSetting passiveSetting;

    // 生效次数
    private int times;

    // 冷却时间
    private int cd;

    // 过期时间
    private int time;

    // 持有者  nullable
    private Unit owner;

    // 添加者  nullable
    private Unit caster;

    // 重写参数
    private Object paramOverride;

    // 附加信息
    private Object addition;

    public PassiveState(PassiveSetting passiveSetting, int time) {
        this.passiveSetting = passiveSetting;
        times = passiveSetting.getTimes();
        if (passiveSetting.getTime() > 0) {
            this.time = time + passiveSetting.getTime();
        } else {
            this.time = Integer.MAX_VALUE;
        }
    }

    public String getId() {
        return passiveSetting.getId();
    }

    public PassiveType getType() {
        return passiveSetting.getType();
    }

    // 扣减可生效次数
    void decreaseTimes() {
        --times;
    }

    public boolean shouldRemove(int time) {
        if (time > this.time) {
            return true;
        }
        if (passiveSetting.getTimes() <= 0) {
            return false;
        }
        return times <= 0;
    }

    public boolean invalid(int time) {
        if (passiveSetting.isSealable() && owner != null && owner.hasState(UnitState.PASSIVE_SEAL, time)) {
            return true;
        }
        if (passiveSetting.getCoolTime() <= 0) {
            return false;
        }
        return cd > time;
    }

    public void addCD(int time) {
        addCD(time, passiveSetting.getCoolTime());
    }

    public void addCD(int time, int cd) {
        decreaseTimes();
        if (cd <= 0) {
            return;
        }
        this.cd = time + cd;
    }

    public <T> T getParam(Class<T> clz) {
        if (paramOverride != null) {
            return (T) paramOverride;
        }
        //noinspection unchecked
        return (T) passiveSetting.getRealParam();
    }

    public <T> T getAddition(Class<T> clz) {
        //noinspection unchecked
        return (T) addition;
    }

    public <T> T getAddition(Class<T> clz, T def) {
        if (addition == null) {
            this.addition = def;
        }
        //noinspection unchecked
        return (T) addition;
    }

}
