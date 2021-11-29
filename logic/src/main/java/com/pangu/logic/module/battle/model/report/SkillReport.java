package com.pangu.logic.module.battle.model.report;

import com.pangu.logic.module.battle.model.AlterAfterValue;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.model.report.values.IValues;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.param.AreaParam;
import com.pangu.framework.protocol.annotation.Transable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 技能战报
 */
@Transable
@Getter
@Setter
public class SkillReport implements IReport, ITimedDamageReport {

    @Getter(AccessLevel.PRIVATE)
    private ReportType type = ReportType.SKILL;
    /**
     * 开始吟唱时间
     */
    private int time;

    /**
     * 抬手动作时间(可能经过攻击加速后，吟唱时间已经与表中配置不一致)
     */
    private int singTime;

    /**
     * 技能ID
     */
    private String skillId;

    /**
     * 释放技能者ID
     */
    private String owner;

    /**
     * 攻击时朝向目标ID
     */
    private String faceTarget;

    /**
     * 被打断时间(为空说明未曾被打断)
     */
    private int brokenTime;

    /**
     * 伤害列表
     */
    private Map<Integer, TimedDamages> damages;

    public static SkillReport sing(int time, String owner, String skillId, int singTime, String faceTarget) {
        SkillReport report = new SkillReport();
        report.time = time;
        report.owner = owner;
        report.skillId = skillId;
        report.singTime = singTime;
        report.faceTarget = faceTarget;
        return report;
    }

    @Override
    public void add(int time, String targetId, IValues value) {
        final IValues procVal = valFilter(value);
        if (procVal == null) {
            return;
        }
        if (damages == null) {
            damages = new LinkedHashMap<>();
        }
        TimedDamages timedDamages = damages.computeIfAbsent(time, TimedDamages::of);
        timedDamages.add(targetId, procVal);
    }

    public void broken(int time) {
        this.brokenTime = time;
    }

    @Override
    public void addAfterValue(int time, Map<String, AlterAfterValue> afterValues) {
        if (afterValues == null || afterValues.isEmpty()) {
            return;
        }
        if (damages == null) {
            damages = new LinkedHashMap<>();
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

    public void clearHp(int time, String unitId) {
        TimedDamages timedDamages = damages.get(time);
        if (timedDamages == null) {
            return;
        }
        timedDamages.clearHp(unitId);
        timedDamages.add(unitId, Hp.of(0));
    }

    @Override
    public void setAreaParam(AreaParam areaParam, int time) {
        if (damages == null) {
            damages = new LinkedHashMap<>();
        }
        final TimedDamages timedDamages = damages.computeIfAbsent(time, TimedDamages::of);
        timedDamages.setEffectArea(areaParam);
    }

    public AreaParam getAreaParam(int time) {
        if (damages == null) {
            damages = new LinkedHashMap<>();
        }
        final TimedDamages timedDamages = damages.computeIfAbsent(time, TimedDamages::of);
        return timedDamages.getEffectArea();
    }
}
