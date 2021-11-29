package com.pangu.logic.module.battle.service.select.select;

import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.BattleConstant;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.select.utils.BestCircle;
import com.pangu.logic.module.battle.service.select.select.utils.Circle;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BestCircleSelect implements Selector {
    @Override
    public List<Unit> select(Unit owner, List<Unit> units, SelectSetting selectSetting, int time) {
        final Point circleCenter = BestCircle.calBestPoint(owner.getPoint(), units.stream().map(Unit::getPoint).collect(Collectors.toList()), selectSetting.getWidth(), BattleConstant.MAX_X, BattleConstant.MAX_Y, false);
        final Circle circle = new Circle(circleCenter.x, circleCenter.y, selectSetting.getWidth());
        return units.stream().filter(unit -> circle.inShape(unit.getPoint().x, unit.getPoint().y)).collect(Collectors.toList());
    }
}
