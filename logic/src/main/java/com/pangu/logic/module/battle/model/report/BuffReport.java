package com.pangu.logic.module.battle.model.report;

import com.pangu.logic.module.battle.model.AlterAfterValue;
import com.pangu.logic.module.battle.model.report.values.IValues;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.param.AreaParam;
import com.pangu.framework.protocol.annotation.Transable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * buff结算或者移除战报
 */
@Transable
@Getter
@Setter
@ToString
public class BuffReport implements IReport, ITimedDamageReport {

    @Getter(AccessLevel.PRIVATE)
    private ReportType type = ReportType.BUF;

    /**
     * 添加buff时间
     */
    private int time;

    /**
     * buffId
     */
    private String buffId;

    /**
     * 全局索引
     */
    private int index;

    /**
     * buff拥有者
     */
    private String owner;

    /**
     * buff释放者
     */
    private String caster;

    /**
     * 移除时间
     */
    private int removeTime = -1;

    /**
     * 伤害列表
     */
    private Map<Integer, TimedDamages> damages;

    /**
     * 是否添加成功
     */
    transient private boolean success;

    public static BuffReport of(int time, String owner, String caster, String buffId) {
        BuffReport r = new BuffReport();
        r.time = time;
        r.buffId = buffId;
        r.owner = owner;
        r.caster = caster;
        return r;
    }

    public void remove(int time) {
        removeTime = time;
    }

    @Override
    public void add(int time, String targetId, IValues values) {
        final IValues procVal = valFilter(values);
        if (procVal == null) {
            return;
        }
        if (damages == null) {
            this.damages = new HashMap<>(4);
        }
        TimedDamages timedDamages = damages.computeIfAbsent(time, TimedDamages::of);
        timedDamages.add(targetId, procVal);
    }

    @Override
    public void addAfterValue(int time, Map<String, AlterAfterValue> afterValues) {
        if (afterValues == null || afterValues.isEmpty()) {
            return;
        }
        if (damages == null) {
            this.damages = new HashMap<>(4);
        }
        TimedDamages timedDamages = damages.computeIfAbsent(time, TimedDamages::of);
        timedDamages.addAfterValue(afterValues);
    }

    @Override
    public List<IValues> queryUnitValue(int time, Unit unit) {
        if (damages == null) {
            return Collections.emptyList();
        }
        TimedDamages timedDamages = damages.get(time);
        if (timedDamages == null) {
            return Collections.emptyList();
        }
        return timedDamages.queryUnitValue(unit);
    }

    @Override
    public void setAreaParam(AreaParam areaParam, int time) {
        final TimedDamages timedDamages = damages.computeIfAbsent(time, TimedDamages::of);
        timedDamages.setEffectArea(areaParam);
    }
}
