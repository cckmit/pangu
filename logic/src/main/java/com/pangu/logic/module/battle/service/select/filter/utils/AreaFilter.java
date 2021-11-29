package com.pangu.logic.module.battle.service.select.filter.utils;

import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.service.BattleConstant;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.select.utils.Circle;
import com.pangu.logic.module.battle.service.select.select.utils.Rectangle;
import com.pangu.logic.module.battle.service.skill.param.AreaParam;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用于获取给定单元集合中，位于指定区域中的单元子集
 */
public class AreaFilter {

    //默认区域参数为攻击方
    public static List<Unit> filterUnitInRelativeArea(List<Unit> units, AreaParam areaParam,Unit owner){
        final boolean isAtk = owner.getFriend().isAttacker();
        if(isAtk){
            return filterUnitInArea(units,areaParam);
        }else {
            return filterUnitInArea(units,calEnemyLocations(areaParam));
        }
    }

    //筛选传入集合中位于指定区域的单元
    public static List<Unit> filterUnitInArea(List<Unit> units, AreaParam areaParam){
        List<Unit> filteredUnits = units;
        switch (areaParam.getShape()) {
            case CIRCLE:
                final int r = areaParam.getR();
                final int[] center = areaParam.getPoints()[0];
                final Circle circle = new Circle(center[0], center[1], r);
                filteredUnits = units.stream().filter(unit -> circle.inShape(unit.getPoint().getX(), unit.getPoint().getY())).collect(Collectors.toList());
                break;
            case RECTANGLE:
                final Point[] points = Arrays.stream(areaParam.getPoints())
                        .map(p -> new Point(p[0], p[1]))
                        .toArray(Point[]::new);
                final Rectangle rectangle = new Rectangle(points);
                filteredUnits = units.stream().filter(unit -> rectangle.inRect(unit.getPoint().getX(), unit.getPoint().getY())).collect(Collectors.toList());
                break;
        }
        return filteredUnits;
    }

    //计算指定区域，相对于场地中线的轴对称坐标
    public static AreaParam calEnemyLocations(AreaParam areaParam){
        final int maxX = BattleConstant.MAX_X;
        final int[][] integers = Arrays.stream(areaParam.getPoints())
                .map(p -> new int[]{maxX - p[0], p[1]})
                .toArray(int[][]::new);

        return new AreaParam(areaParam.getShape(),integers,areaParam.getR());
    }


}
