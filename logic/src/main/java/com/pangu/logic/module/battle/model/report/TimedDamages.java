package com.pangu.logic.module.battle.model.report;

import com.pangu.logic.module.battle.model.AlterAfterValue;
import com.pangu.logic.module.battle.model.report.values.*;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.param.AreaParam;
import com.pangu.framework.protocol.annotation.Transable;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * 伤害战报
 */
@Transable
@Getter
public class TimedDamages {

    /**
     * 生效时间
     */
    private int time;

    @Setter
    private AreaParam effectArea;

    /**
     * key->目标ID，value->伤害类型集合
     */
    private Map<String, List<IValues>> values;

    /**
     * 变更后的值
     */
    private Map<String, AlterAfterValue> afterValues;

    public static TimedDamages of(int time) {
        TimedDamages d = new TimedDamages();
        d.time = time;
        return d;
    }

    public void add(String targetId, IValues value) {
        if (this.values == null) {
            this.values = new HashMap<>(4);
        }
        List<IValues> values = this.values.computeIfAbsent(targetId, k -> new ArrayList<>(3));
        values.add(value);
    }


    public void addAfterValue(Map<String, AlterAfterValue> afterValues) {
        if (afterValues == null || afterValues.isEmpty()) {
            return;
        }
        if (this.afterValues == null) {
            this.afterValues = new HashMap<>(6);
        }
        for (Map.Entry<String, AlterAfterValue> entry : afterValues.entrySet()) {
            String id = entry.getKey();
            AlterAfterValue val = entry.getValue();
            if (val == null) {
                continue;
            }
            AlterAfterValue afterValue = this.afterValues.computeIfAbsent(id, k -> val);
            if (afterValue == val) {
                continue;
            }
            afterValue.merge(val);
        }
    }

    public List<IValues> queryUnitValue(Unit unit) {
        if (values == null) {
            return Collections.emptyList();
        }
        return values.getOrDefault(unit.getId(), Collections.emptyList());
    }

    public void clearHp(String unitId) {
        if (values == null) {
            return;
        }
        List<IValues> iValues = values.get(unitId);
        if (iValues == null) {
            return;
        }
        iValues.removeIf(next -> {
            if (next instanceof PassiveValue) {
                List<IValues> passiveValues = ((PassiveValue) next).getValues();
                if (passiveValues == null) {
                    return false;
                }
                if (passiveValues.size() == 1) {
                    IValues passiveValueItem = passiveValues.get(0);
                    return (passiveValueItem instanceof Hp) && ((Hp) passiveValueItem).getDamage() < 0;
                } else {
                    passiveValues.removeIf(item -> (item instanceof Hp) && ((Hp) item).getDamage() < 0);
                    return passiveValues.size() == 0;
                }
            }
            return (next instanceof Hp) && ((Hp) next).getDamage() < 0;
        });
    }
}
