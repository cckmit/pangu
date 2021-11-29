package com.pangu.logic.module.battle.model;

import com.pangu.framework.protocol.annotation.Transable;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 某个单元战斗数据统计
 */
@Transable
@Getter
public class UnitReport {

    /**
     * 攻击其他单位伤害
     */
    private long attack;

    /**
     * 承受治疗量
     */
    private long recover;

    /**
     * 承受伤害量
     */
    private long defence;

    /**
     * 击杀记录
     */
    private List<UnitKillReport> kills;

    public void addAttack(long value) {
        this.attack += value;
    }

    public void addDefence(long value) {
        this.defence += value;
    }

    public void addRecover(String id, long value) {
        this.recover += value;
    }

    public void addKilled(String id, int time) {
        if (kills == null) {
            kills = new ArrayList<>(6);
        }
        this.kills.add(new UnitKillReport(id, time));
    }

    public UnitReport clearForSave() {
        if (kills == null) {
            return this;
        }
        this.kills.clear();
        this.kills = null;
        return this;
    }
}
