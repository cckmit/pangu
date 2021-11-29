package com.pangu.logic.module.battle.model.report;

import com.pangu.logic.module.battle.model.AlterAfterValue;
import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.model.report.values.IValues;
import com.pangu.logic.module.battle.model.report.values.Mp;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.param.AreaParam;

import java.util.List;
import java.util.Map;

/**
 * 时间点伤害战报操作集合
 * 暂时存在SkillReport,TargetReport
 */
public interface ITimedDamageReport {

    /**
     * @return 战报主体
     */
    String getOwner();

    /**
     * 添加属性变更
     *
     * @param time
     * @param targetId
     * @param value
     */
    void add(int time, String targetId, IValues value);

    /**
     * 记录属性变更后的值
     *
     * @param time
     * @param afterValues
     */
    void addAfterValue(int time, Map<String, AlterAfterValue> afterValues);

    /**
     * 查询某个时间点，某个英雄属性变更列表
     *
     * @param time
     * @param unit
     * @return
     */
    List<IValues> queryUnitValue(int time, Unit unit);

    /**
     * 返回伤害列表
     *
     * @return
     */
    Map<Integer, TimedDamages> getDamages();

    /**
     * 合并两个战报
     *
     * @param report
     */
    default void mergeDamages(ITimedDamageReport report) {
        Map<Integer, TimedDamages> damages = report.getDamages();
        for (Map.Entry<Integer, TimedDamages> entry : damages.entrySet()) {
            Integer curTime = entry.getKey();
            TimedDamages v = entry.getValue();
            Map<String, List<IValues>> values = v.getValues();
            for (Map.Entry<String, List<IValues>> e : values.entrySet()) {
                String tar = e.getKey();
                List<IValues> valuesList = e.getValue();
                for (IValues item : valuesList) {
                    add(curTime, tar, item);
                }
            }
        }
    }

    void setAreaParam(AreaParam areaParam,int time);

    default IValues valFilter(IValues value) {
        if (value instanceof UnitValues) {//血量变更统一使用Hp类型
            UnitValues unitValues = (UnitValues) value;
            if (unitValues.getAlterType() == AlterType.HP) {
                return Hp.of(unitValues.getValue().longValue());
            }
            if (unitValues.getAlterType() == AlterType.MP) {
                return new Mp(unitValues.getValue().longValue());
            }
            return null;
        } else {
            return value;
        }
    }
}
