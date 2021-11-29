package com.pangu.logic.module.battle.service.select.select;

import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.core.Fighter;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.select.utils.TwoPointDistanceUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 朝目标发射两道弧形运动罡风，对路径上的敌军造成80%的物理攻击的伤害。当罡风汇聚时，它们会合成一股巨型龙卷风
 * ;
 * 直线距离
 */
public class DistanceCircle implements Selector {
    @Override
    public List<Unit> select(Unit owner, List<Unit> units, SelectSetting selectSetting, int time) {
        Unit target = owner.getTarget();
        if (target == null) {
            return Collections.emptyList();
        }
        Point ownerPoint = owner.getPoint();
        Point targetPoint = target.getPoint();
        int distance = ownerPoint.distance(targetPoint);
        int needLength = selectSetting.getDistance();
        int diff = needLength - distance;
        Point validPoint;
        if (diff < 0) {
            validPoint = TwoPointDistanceUtils.getNearStartPoint(ownerPoint, needLength, targetPoint);
        } else {
            if (diff == 0) {
                validPoint = targetPoint;
            } else {
                validPoint = TwoPointDistanceUtils.getNearEndPointDistance(ownerPoint, targetPoint, diff);
            }
        }
        Fighter enemy = owner.getEnemy();
        List<Unit> current = enemy.getCurrent();
        List<Unit> validUnits = new ArrayList<>(current.size());
        for (Unit item : current) {
            if (item.getPoint().distance(validPoint) <= selectSetting.getWidth()) {
                validUnits.add(item);
            }
        }
        return validUnits;
    }
}
