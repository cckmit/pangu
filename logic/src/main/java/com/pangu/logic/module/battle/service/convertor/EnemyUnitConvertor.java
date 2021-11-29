package com.pangu.logic.module.battle.service.convertor;

import com.pangu.logic.module.battle.model.FighterType;
import com.pangu.logic.module.battle.model.UnitBuildInfo;
import com.pangu.logic.module.battle.service.core.Fighter;
import com.pangu.logic.module.battle.service.core.Unit;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 配置敌军的战斗单位转换器
 */
@Component
public class EnemyUnitConvertor implements FighterConvertor<UnitBuildInfo[][]> {

    @Override
    public FighterType getType() {
        return FighterType.ENEMY_UNIT;
    }

    @Override
    public Fighter convert(UnitBuildInfo[][] enemies, boolean isAttacker, int index) {
        List<Unit> units = new ArrayList<>(6);
        int sequence = 0;
        for (UnitBuildInfo[] rowEnemyIds : enemies) {
            for (UnitBuildInfo enemyId : rowEnemyIds) {
                if (enemyId == null) {
                    ++sequence;
                    continue;
                }
                String unitId = Unit.toUnitId(isAttacker, sequence);
                Unit unit = Unit.valueOf(
                        enemyId.getModel(),
                        enemyId.getValues(),
                        enemyId.getRates(),
                        enemyId.getState(),
                        enemyId.getSkills(),
                        enemyId.getPassives());
                unit.setId(unitId);
                units.add(unit);
                unit.setSequence(sequence++);
            }
        }
        return Fighter.valueOf(units, isAttacker);
    }

    @Override
    public int getBattleTimes(UnitBuildInfo[][] id) {
        return 1;
    }

    @Override
    public Integer getWinTimes(UnitBuildInfo[][] id) {
        return 1;
    }
}
