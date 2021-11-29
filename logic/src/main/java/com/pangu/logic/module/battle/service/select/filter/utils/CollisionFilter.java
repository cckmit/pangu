package com.pangu.logic.module.battle.service.select.filter.utils;

import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.select.utils.Rectangle;

import java.util.*;

/**
 * 用于过滤目标集合中未被过滤的目标
 */
public class CollisionFilter {
    public static List<Unit> calExposedUnits(List<Unit> targets, Unit ref, int width ,int length) {
        final Set<Unit> filtered = new HashSet<>(targets.size());
        for (Unit target : targets) {
            final Rectangle rectangle = new Rectangle(ref.getPoint(), target.getPoint(), width, length);
            final List<Unit> inRects = new ArrayList<>();
            for (Unit t : targets) {
                if (rectangle.inRect(t.getPoint().x, t.getPoint().y)) {
                    inRects.add(t);
                }
            }
            inRects.sort(new Comparator<Unit>() {
                @Override
                public int compare(Unit o1, Unit o2) {
                    return ref.getPoint().distance(o1.getPoint()) - ref.getPoint().distance(o2.getPoint());
                }
            });
            filtered.add(inRects.get(0));
        }
        return new ArrayList<>(filtered);
    }
}
