package com.pangu.logic.module.battle.service.select.select;

import com.pangu.logic.module.battle.model.AreaType;
import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.select.utils.FanShaped;
import com.pangu.logic.module.battle.service.skill.param.AreaParam;

import java.util.ArrayList;
import java.util.List;

/**
 * 根据传入的坐标而非角色faceTarget来固定扇形方向
 */
public class CoordinateFanShapedSelect implements Selector {
    @Override
    public List<Unit> select(Unit owner, List<Unit> units, SelectSetting selectSetting, int time) {
        final ArrayList<Unit> targets = new ArrayList<>(units.size());
        if (selectSetting.getRealParam() instanceof AreaParam) {
            final AreaParam areaParam = selectSetting.getRealParam(AreaParam.class);
            if (areaParam.getShape() == AreaType.POINT) {
                final Point target = new Point(areaParam.getPoints()[0][0], areaParam.getPoints()[0][1]);
                FanShaped fanShaped = new FanShaped(owner.getPoint(), target, selectSetting.getWidth(), selectSetting.getDistance());
                for (Unit unit : units) {
                    Point pos = unit.getPoint();
                    if (fanShaped.inShape(pos.getX(), pos.getY())) {
                        targets.add(unit);
                    }
                }
            }
        }
        return targets;
    }
}
