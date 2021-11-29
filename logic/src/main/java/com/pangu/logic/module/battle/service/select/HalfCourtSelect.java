package com.pangu.logic.module.battle.service.select;

import com.pangu.logic.module.battle.model.FilterType;
import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.BattleConstant;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.select.Selector;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 选择敌方或我方半场所包含的目标
 */
public class HalfCourtSelect implements Selector {
    private final int HALF_COURT_X = BattleConstant.MAX_X / 2;

    @Override
    public List<Unit> select(Unit owner, List<Unit> units, SelectSetting selectSetting, int time) {
        if (CollectionUtils.isEmpty(units)) {
            return Collections.emptyList();
        }

        final FilterType whichHalfCourt = selectSetting.getRealParam(FilterType.class);
        final boolean attacker = owner.getFriend().isAttacker();
        final boolean friendHalfCourt = whichHalfCourt == FilterType.FRIEND;

        final List<Unit> inArea = new ArrayList<>(12);
        //  进攻方的己方半场与防守方的敌方半场等价
        if (attacker && friendHalfCourt || !attacker && !friendHalfCourt) {
            for (Unit unit : units) {
                if (unit.getPoint().x < HALF_COURT_X) {
                    inArea.add(unit);
                }
            }
        } else {
            for (Unit unit : units) {
                if (unit.getPoint().x >= HALF_COURT_X) {
                    inArea.add(unit);
                }
            }
        }
        return inArea;
    }
}
