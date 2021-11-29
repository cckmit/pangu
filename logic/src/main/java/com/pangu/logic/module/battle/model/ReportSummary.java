package com.pangu.logic.module.battle.model;

import com.pangu.logic.module.battle.model.report.BuffReport;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.TimedDamages;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.model.report.values.IValues;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.framework.protocol.annotation.Transable;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Transable
@Getter
public class ReportSummary {

    /**
     * 战报地址
     */
    private String reportPath;

    /**
     * 所有战斗单元
     */
    private Map<String, ModelInfo> units;

    /**
     * 每个单位的伤害统计
     */
    private Map<String, UnitReport> reports;

    public void parse(BuffReport buffReport, Map<String, String> summerMap) {
        if (buffReport == null) {
            return;
        }
        String caster = buffReport.getCaster();
        caster = summerMap.getOrDefault(caster, caster);
        Map<Integer, TimedDamages> damages = buffReport.getDamages();
        parseDamages(caster, damages);
    }

    public void parse(SkillReport skillReport, Map<String, String> summerMap) {
        if (skillReport == null) {
            return;
        }
        String owner = skillReport.getOwner();
        owner = summerMap.getOrDefault(owner, owner);
        Map<Integer, TimedDamages> damages = skillReport.getDamages();
        parseDamages(owner, damages);
    }

    private void parsePassiveDmg(PassiveValue passiveValue, String targetId) {
        // 并非所有被动都包含数据列表，部分被动仅用于供前端做特效表现
        final List<IValues> values = passiveValue.getValues();
        if (CollectionUtils.isEmpty(values)) {
            return;
        }

        final String owner = passiveValue.getOwner();
        // 目前仅统计被动中的伤害和恢复系效果
        for (IValues value : values) {
            if (!(value instanceof Hp)) {
                continue;
            }
            final Hp hp = (Hp) value;
            long changeValue = hp.getDamage();
            // 伤害系被动
            if (changeValue < 0) {
                changeValue = -changeValue;
                addAttack(owner, changeValue);
                addDefence(targetId, changeValue);
            } else {
                // 回复系被动
                if (hp.isRecover()) {
                    addRecover(owner, changeValue);
                }
            }
        }
    }

    private void parseDamages(String owner, Map<Integer, TimedDamages> damages) {
        if (damages == null) {
            return;
        }
        for (Map.Entry<Integer, TimedDamages> entry : damages.entrySet()) {
            Integer time = entry.getKey();
            TimedDamages timedDamages = entry.getValue();
            Map<String, List<IValues>> values = timedDamages.getValues();
            if (values != null) {
                for (Map.Entry<String, List<IValues>> e : values.entrySet()) {
                    String targetId = e.getKey();
                    List<IValues> valueChange = e.getValue();
                    for (IValues item : valueChange) {
                        if (item instanceof PassiveValue) {
                            parsePassiveDmg((PassiveValue) item, targetId);
                            continue;
                        }
                        if (!(item instanceof Hp)) {
                            continue;
                        }
                        long changeValue = ((Hp) item).getDamage();
                        // 攻击方造成伤害
                        if (changeValue < 0) {
                            changeValue = -changeValue;
                            addAttack(owner, changeValue);
                            addDefence(targetId, changeValue);
                        } else {
                            addRecover(owner, changeValue);
                        }
                    }
                }
            }
            Map<String, AlterAfterValue> afterValues = timedDamages.getAfterValues();
            summaryKilled(owner, time, afterValues);
        }
    }

    private void summaryKilled(String owner, int time, Map<String, AlterAfterValue> currentValue) {
        if (currentValue != null) {
            for (Map.Entry<String, AlterAfterValue> entry : currentValue.entrySet()) {
                String unitId = entry.getKey();
                AlterAfterValue afterValue = entry.getValue();
                Long currentHp = afterValue.getValue(UnitValue.HP);
                if (currentHp != null && currentHp <= 0) {
                    addKilled(owner, unitId, time);
                }
            }
        }
    }

    private void addKilled(String owner, String unitId, int time) {
        UnitReport unitReport = loadUnitReport(owner);
        unitReport.addKilled(unitId, time);
    }

    private void addRecover(String id, Long value) {
        UnitReport unitReport = loadUnitReport(id);
        unitReport.addRecover(id, value);
    }

    private UnitReport loadUnitReport(String id) {
        if (reports == null) {
            reports = new HashMap<>(this.units.size());
        }
        return reports.computeIfAbsent(id, k -> new UnitReport());
    }

    private void addDefence(String id, Long value) {
        UnitReport unitReport = loadUnitReport(id);
        unitReport.addDefence(value);
    }

    private void addAttack(String id, long value) {
        UnitReport unitReport = loadUnitReport(id);
        unitReport.addAttack(value);
    }

    public void initUnit(FighterInfo attacker, FighterInfo defender) {
        this.units = new HashMap<>(12);
        List<UnitInfo> attackCurrent = attacker.getCurrent();
        for (UnitInfo unitInfo : attackCurrent) {
            units.put(unitInfo.getId(), unitInfo.getModel());
        }
        List<UnitInfo> defendCurrent = defender.getCurrent();
        for (UnitInfo unitInfo : defendCurrent) {
            units.put(unitInfo.getId(), unitInfo.getModel());
        }
    }

    public ReportSummary clearForSave() {
        if (reports == null) {
            return this;
        }
        for (Map.Entry<String, UnitReport> entry : reports.entrySet()) {
            UnitReport value = entry.getValue();
            if (value == null) {
                continue;
            }
            value.clearForSave();
        }
        return this;
    }
}
