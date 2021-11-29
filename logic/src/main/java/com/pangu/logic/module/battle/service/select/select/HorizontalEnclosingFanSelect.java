package com.pangu.logic.module.battle.service.select.select;

import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.BattleConstant;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.select.utils.FanShaped;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ∞ 型区域，owner位于 ∞ 中心
 */
@Component
public class HorizontalEnclosingFanSelect implements Selector {
    @Override
    public List<Unit> select(Unit owner, List<Unit> units, SelectSetting selectSetting, int time) {
        if (CollectionUtils.isEmpty(units)) {
            return Collections.emptyList();
        }

        //获取扇形角度和半径长度
        final int radius = selectSetting.getDistance();
        final int angle = selectSetting.getWidth();

        //获取用于固定扇形区域的关键坐标
        final Point ownerPoint = owner.getPoint();
        final Point targetPoint1 = new Point(BattleConstant.MAX_X, ownerPoint.y);
        final Point targetPoint2 = new Point(0, ownerPoint.y);

        //构建扇形
        final FanShaped fanShaped1 = new FanShaped(owner.getPoint(), targetPoint1, angle, radius);
        final FanShaped fanShaped2 = new FanShaped(owner.getPoint(), targetPoint2, angle, radius);

        //筛选位于沙漏中的目标
        final ArrayList<Unit> targetsInFanShaped1 = new ArrayList<>(units.size());
        final ArrayList<Unit> targetsInFanShaped2 = new ArrayList<>(units.size());
        for (Unit unit : units) {
            final int x = unit.getPoint().x;
            final int y = unit.getPoint().y;
            if (fanShaped1.inShape(x, y)) {
                targetsInFanShaped1.add(unit);
            }
            if (fanShaped2.inShape(x, y)) {
                targetsInFanShaped2.add(unit);
            }
        }
        return targetsInFanShaped1.size() >= targetsInFanShaped2.size() ? targetsInFanShaped1 : targetsInFanShaped2;
    }
}
