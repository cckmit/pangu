package com.pangu.logic.module.battle.service.select.select.utils;

import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.service.core.Unit;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.List;

/**
 * 以起点和终点的连线固定一个矩形，在给定矩形长和宽的前提下，确定一个矩形四个角的坐标数组，使得落在该矩形内的单位数量最多
 */
public class BestRectangle {
    //计算最佳矩形
    public static UnitInfo calBestRectangle(Unit owner, List<Unit> targets, int width, int length) {
        final List<UnitInfo> infos = new ArrayList<>(targets.size());
        for (Unit target : targets) {
            final UnitInfo info = new UnitInfo();
            final Rectangle rectangle = new Rectangle(owner.getPoint(), target.getPoint(), width, length);
            for (Unit unit : targets) {
                final boolean inRect = rectangle.inRect(unit.getPoint().x, unit.getPoint().y);
                if (inRect) info.inRect.add(unit);
            }
            info.rectangle = rectangle;
            info.target = target;
            infos.add(info);
        }
        if (CollectionUtils.isEmpty(infos)) {
            return null;
        }
        infos.sort(BestRectangle::compare);
        return infos.get(0);
    }

    public static PointInfo calBestRectangle(Point startPoint, Point[] points, int width, int length) {
        final List<PointInfo> infos = new ArrayList<>(points.length);
        for (Point point : points) {
            final PointInfo info = new PointInfo();
            final Rectangle rectangle = new Rectangle(startPoint, point, width, length);
            for (Point p : points) {
                final boolean inRect = rectangle.inRect(p.x, p.y);
                if (inRect) info.inRectCount++;
            }
            info.rectangle = rectangle;
            info.target = point;
            infos.add(info);
        }
        if (CollectionUtils.isEmpty(infos)) {
            return null;
        }
        infos.sort(BestRectangle::compare);
        return infos.get(0);
    }

    private static int compare(UnitInfo o1, UnitInfo o2) {
        return o2.inRect.size() - o1.inRect.size();
    }

    private static int compare(PointInfo o1, PointInfo o2) {
        return o2.inRectCount - o1.inRectCount;
    }

    @Getter
    public static class UnitInfo {
        private Unit target;
        private List<Unit> inRect = new ArrayList<>();
        private Rectangle rectangle;
    }
    @Getter
    public static class PointInfo {
        private Point target;
        private int inRectCount;
        private Rectangle rectangle;
    }

}
