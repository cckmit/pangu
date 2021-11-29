package com.pangu.logic.module.battle.service.convertor;

import com.pangu.logic.module.battle.model.FighterType;
import com.pangu.logic.module.battle.resource.EnemyUnitSetting;
import com.pangu.logic.module.battle.service.EnemyUnitReader;
import com.pangu.logic.module.battle.service.core.Fighter;
import com.pangu.logic.module.battle.service.core.Unit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 配置敌军的战斗单位转换器
 */
@Component
public class EnemyIdConvertor implements FighterConvertor<String[]> {

    @Autowired
    private EnemyUnitReader enemyUnitReader;

    @Override
    public FighterType getType() {
        return FighterType.ENEMY_UNIT_ID;
    }

    @Override
    public Fighter convert(String[] ids, boolean isAttacker, int index) {
        List<Unit> units = new ArrayList<>(6);
        int sequence = 0;
        for (String id : ids) {
            if (StringUtils.isEmpty(id)) {
                ++sequence;
                continue;
            }
            EnemyUnitSetting enemy = enemyUnitReader.get(id, true);
            String unitId = Unit.toUnitId(isAttacker, sequence);
            Unit unit = enemy.toUnit(unitId, null);
            units.add(unit);
            unit.setSequence(sequence++);
        }
        return Fighter.valueOf(units, isAttacker);
    }

    @Override
    public int getBattleTimes(String[] id) {
        return 1;
    }

    @Override
    public Integer getWinTimes(String[] id) {
        return 1;
    }
}
