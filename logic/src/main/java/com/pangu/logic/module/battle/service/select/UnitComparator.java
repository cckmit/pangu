package com.pangu.logic.module.battle.service.select;

import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.service.core.Unit;

import java.util.Comparator;

/**
 * 战斗单位比较器
 *
 * @author junlei.xie
 */
public final class UnitComparator {

    //  射程排序器
    public static final Comparator<Unit> RANGE_COMPARATOR = new Comparator<Unit>() {
        @Override
        public int compare(Unit a1, Unit a2) {
            int value = a1.getNormalSkill().getRange() - a2.getNormalSkill().getRange();
            if (value != 0) {
                return value;
            }
            return a1.getId().compareTo(a2.getId());
        }
    };

    //  速度比较器
    public static final Comparator<Unit> COMPARATOR_SPEED = new Comparator<Unit>() {
        @Override
        public int compare(Unit o1, Unit o2) {
            int result = Long.compare(o2.getValue(UnitValue.SPEED), o1.getValue(UnitValue.SPEED));
            if (result != 0) {
                return result;
            }
            return o1.getId().compareTo(o2.getId());
        }
    };

    //  血量百分比比较器
    public static final Comparator<Unit> COMPARATOR_SACAL_HP_MAX = new Comparator<Unit>() {
        @Override
        public int compare(Unit o1, Unit o2) {
            double hp1 = (double) o1.getValue(UnitValue.HP) / o1.getValue(UnitValue.HP_MAX);
            double hp2 = (double) o2.getValue(UnitValue.HP) / o2.getValue(UnitValue.HP_MAX);
            int result = Double.compare(hp1, hp2);
            if (result != 0) {
                return -result;
            }
            return o1.getId().compareTo(o2.getId());
        }
    };

    //  血量百分比比较器
    public static final Comparator<Unit> COMPARATOR_SACAL_HP_MIN = new Comparator<Unit>() {
        @Override
        public int compare(Unit o1, Unit o2) {
            double hp1 = (double) o1.getValue(UnitValue.HP) / o1.getValue(UnitValue.HP_MAX);
            double hp2 = (double) o2.getValue(UnitValue.HP) / o2.getValue(UnitValue.HP_MAX);
            int result = Double.compare(hp1, hp2);
            if (result != 0) {
                return result;
            }
            return o1.getId().compareTo(o2.getId());
        }
    };

    //  力量比较器
    public static final Comparator<Unit> COMPARATOR_STRENGTH_MAX = new Comparator<Unit>() {
        @Override
        public int compare(Unit o1, Unit o2) {
            int result = Long.compare(o2.getValue(UnitValue.STRENGTH), o1.getValue(UnitValue.STRENGTH));
            if (result != 0) {
                return result;
            }
            return o1.getId().compareTo(o2.getId());
        }
    };

    //  力量比较器
    public static final Comparator<Unit> COMPARATOR_STRENGTH_MIN = new Comparator<Unit>() {
        @Override
        public int compare(Unit o1, Unit o2) {
            int result = Long.compare(o1.getValue(UnitValue.STRENGTH), o2.getValue(UnitValue.STRENGTH));
            if (result != 0) {
                return result;
            }
            return o1.getId().compareTo(o2.getId());
        }
    };

    //  智力比较器
    public static final Comparator<Unit> COMPARATOR_INTELLECT_MAX = new Comparator<Unit>() {
        @Override
        public int compare(Unit o1, Unit o2) {
            int result = Long.compare(o2.getValue(UnitValue.INTELLECT), o1.getValue(UnitValue.INTELLECT));
            if (result != 0) {
                return result;
            }
            return o1.getId().compareTo(o2.getId());
        }
    };

    //  智力比较器
    public static final Comparator<Unit> COMPARATOR_INTELLECT_MIN = new Comparator<Unit>() {
        @Override
        public int compare(Unit o1, Unit o2) {
            int result = Long.compare(o1.getValue(UnitValue.INTELLECT), o2.getValue(UnitValue.INTELLECT));
            if (result != 0) {
                return result;
            }
            return o1.getId().compareTo(o2.getId());
        }
    };

    //  敏捷比较器
    public static final Comparator<Unit> COMPARATOR_AGILITY_MAX = new Comparator<Unit>() {
        @Override
        public int compare(Unit o1, Unit o2) {
            int result = Long.compare(o2.getValue(UnitValue.AGILITY), o1.getValue(UnitValue.AGILITY));
            if (result != 0) {
                return result;
            }
            return o1.getId().compareTo(o2.getId());
        }
    };

    //  敏捷比较器
    public static final Comparator<Unit> COMPARATOR_AGILITY_MIN = new Comparator<Unit>() {
        @Override
        public int compare(Unit o1, Unit o2) {
            int result = Long.compare(o1.getValue(UnitValue.AGILITY), o2.getValue(UnitValue.AGILITY));
            if (result != 0) {
                return result;
            }
            return o1.getId().compareTo(o2.getId());
        }
    };

    //  射程比较器
    public static final Comparator<Unit> COMPARATOR_RANGE = new Comparator<Unit>() {
        @Override
        public int compare(Unit o1, Unit o2) {
            int result = o1.getNormalSkill().getRange() - o2.getNormalSkill().getRange();
            if (result != 0) {
                return -result;
            }
            if (o1.getId().startsWith(Unit.ATTACKER_PREFIX) && o2.getId().startsWith(Unit.DEFENDER_PREFIX)) {
                return -1;
            }
            if (o1.getId().startsWith(Unit.DEFENDER_PREFIX) && o2.getId().startsWith(Unit.ATTACKER_PREFIX)) {
                return 1;
            }
            return o1.getId().compareTo(o2.getId());
        }
    };
}
