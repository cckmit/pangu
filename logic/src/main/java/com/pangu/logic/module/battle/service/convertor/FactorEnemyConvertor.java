package com.pangu.logic.module.battle.service.convertor;

import com.pangu.logic.module.battle.model.FactorEnemyBuildInfo;
import com.pangu.logic.module.battle.model.FighterType;
import com.pangu.logic.module.battle.resource.EnemyFighterSetting;
import com.pangu.logic.module.battle.resource.EnemyGroupSetting;
import com.pangu.logic.module.battle.resource.EnemyUnitSetting;
import com.pangu.logic.module.battle.service.EnemyUnitReader;
import com.pangu.logic.module.battle.service.core.Fighter;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.framework.resource.Storage;
import com.pangu.framework.resource.anno.Static;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FactorEnemyConvertor implements FighterConvertor<FactorEnemyBuildInfo> {
    @Static
    private Storage<String, EnemyFighterSetting> fighterStorage;

    @Static
    private Storage<String, EnemyGroupSetting> groupStorage;

    @Autowired
    private EnemyUnitReader enemyUnitReader;

    @Override
    public FighterType getType() {
        return FighterType.FACTOR_ENEMY;
    }

    @Override
    public Fighter convert(FactorEnemyBuildInfo info, boolean isAttacker, int index) {
        EnemyFighterSetting enemyFighterSetting = fighterStorage.get(info.getEnemyId(), true);
        String[] groups = enemyFighterSetting.getGroups();
        String groupId = groups[index];

        EnemyGroupSetting enemyGroupSetting = groupStorage.get(groupId, true);
        final String[] skills = enemyGroupSetting.getSkills();
        List<Unit> units = new ArrayList<>(6);
        int sequence = 0;
        String[] names = enemyGroupSetting.getNames();
        String[] enemies = enemyGroupSetting.getEnemies();
        for (int i = 0; i < enemies.length; ++i) {
            String name = null;
            if (names != null) {
                name = names[i];
            }
            String enemyId = enemies[i];
            if (enemyId == null || enemyId.isEmpty()) {
                ++sequence;
                continue;
            }
            EnemyUnitSetting enemy = enemyUnitReader.get(enemyId, true);
            String unitId = Unit.toUnitId(isAttacker, sequence);
            Unit unit;
            if (i == 0 && ArrayUtils.isNotEmpty(skills)) {
                unit = enemy.toUnit(unitId, name, skills, info.getFactor());
            } else {
                unit = enemy.toUnit(unitId, name, null, info.getFactor());
            }
            units.add(unit);
            unit.setSequence(sequence++);
        }
        return Fighter.valueOf(units, isAttacker);
    }

    @Override
    public int getBattleTimes(FactorEnemyBuildInfo info) {
        EnemyFighterSetting enemyFighterSetting = fighterStorage.get(info.getEnemyId(), true);
        String[] groups = enemyFighterSetting.getGroups();
        return groups.length;
    }

    @Override
    public Integer getWinTimes(FactorEnemyBuildInfo info) {
        EnemyFighterSetting enemyFighterSetting = fighterStorage.get(info.getEnemyId(), true);
        final Integer winTimes = enemyFighterSetting.getWinTimes();
        if (winTimes == null) {
            return enemyFighterSetting.getGroups().length;
        }
        return winTimes;
    }
}
