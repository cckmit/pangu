package com.pangu.logic.module.battle.service.select.select;

import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.core.Unit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 临近站位单元选择器
 */
public class AdjoinSequenceSelect implements Selector {
    private static List<Integer> front = Arrays.asList(0, 1);

    private static List<Integer> back = Arrays.asList(2, 3, 4, 5);

    @Override
    public List<Unit> select(Unit owner, List<Unit> units, SelectSetting selectSetting, int time) {
        final Integer[] around = getAround(owner);
        if (around == null) {
            return null;
        }
        List<Unit> selects = new ArrayList<>(around.length);
        for (Unit unit : units) {
            for (Integer integer : around) {
                if (unit.getSequence() == integer) {
                    selects.add(unit);
                }
            }
        }
        return selects;
    }

    private Integer[] getAround(Unit owner) {
        final int sequence = owner.getSequence();
        int i = front.indexOf(sequence);
        if (i >= 0) {
            final Integer[] round = getRound(front, i - 1, i + 1);
            if (round != null) {
                return round;
            }
        }
        i = back.indexOf(sequence);
        if (i >= 0) {
            final Integer[] round = getRound(back, i - 1, i + 1);
            return round;
        }
        return null;
    }

    private Integer[] getRound(List<Integer> list, int left, int right) {
        if (left < 0 && right >= list.size()) {
            return null;
        }
        if (left > 0 && right < list.size()) {
            return new Integer[]{list.get(left), list.get(right)};
        }
        if (left < 0) {
            return new Integer[]{list.get(right)};
        } else {
            return new Integer[]{list.get(left)};
        }
    }
}
