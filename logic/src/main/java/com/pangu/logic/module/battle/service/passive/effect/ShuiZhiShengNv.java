package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.FilterType;
import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillReleasePassive;
import com.pangu.logic.module.battle.service.passive.param.ShuiZhiShengNvParam;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 使我方全体下次怒攻伤害提升30%
 */
@Component("PASSIVE:ShuiZhiShengNv")
public class ShuiZhiShengNv implements SkillReleasePassive {
    @Override
    public PassiveType getType() {
        return PassiveType.SHUI_ZHI_SHENG_NV;
    }

    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time, Context context, SkillReport skillReport) {
        if (owner.getFriend() != attacker.getFriend()) {
            return;
        }

        final Addition addition = passiveState.getAddition(Addition.class, new Addition());
        final ShuiZhiShengNvParam param = passiveState.getParam(ShuiZhiShengNvParam.class);
        final int triggerTimes = param.getAvaibaleTimes();

        if (triggerTimes <= 0) {
            return;
        }

        //  自己释放技能时，为队友强化下几次大招
        if (owner == attacker) {
            final List<Unit> friends = FilterType.FRIEND.filter(owner, time);
            addition.charge(friends, triggerTimes);
            addition.activated = true;
            return;
        }

        //  提升性能
        if (!addition.activated) {
            return;
        }

        //  队友释放大招时，根据状态决定是为其增加大招伤害，或者重置大招伤害加成，或者不做任何处理
        if (skillState.getType() != SkillType.SPACE) {
            return;
        }

        if (addition.consume(attacker) && !addition.buffed(attacker)) {
            addition.unitBuffed.put(attacker, true);

            final double spaceAddRate = param.getSpaceAddRate();
            context.addPassiveValue(attacker, AlterType.RATE_SPACE_ADD, spaceAddRate);
            skillReport.add(time, attacker.getId(), PassiveValue.single(passiveState.getId(), owner.getId(), new UnitValues(AlterType.RATE_SPACE_ADD, spaceAddRate)));
        } else if (addition.needReset(attacker)) {
            addition.unitBuffed.put(attacker, false);

            final double spaceAddRate = param.getSpaceAddRate();
            context.addPassiveValue(attacker, AlterType.RATE_SPACE_ADD, -spaceAddRate);
            skillReport.add(time, attacker.getId(), PassiveValue.single(passiveState.getId(), owner.getId(), new UnitValues(AlterType.RATE_SPACE_ADD, -spaceAddRate)));
            addition.unitSpaceCount.remove(attacker);
            //提升性能
            if (addition.unitSpaceCount.size() == 0) {
                addition.activated = false;
            }
        }
    }

    private static class Addition {
        private boolean activated;
        private final Map<Unit, Integer> unitSpaceCount = new HashMap<>(8);
        private final Map<Unit, Boolean> unitBuffed = new HashMap<>(8);

        public void charge(List<Unit> units, int chargeTimes) {
            for (Unit unit : units) {
                unitSpaceCount.put(unit, chargeTimes);
            }
        }

        public boolean buffed(Unit unit) {
            return unitBuffed.computeIfAbsent(unit, k -> false);
        }

        public boolean consume(Unit unit) {
            Integer consumable = unitSpaceCount.get(unit);
            if (consumable == null) {
                return false;
            }

            consumable--;
            if (consumable < 0) {
                return false;
            }

            unitSpaceCount.put(unit, consumable);
            return true;
        }

        public boolean needReset(Unit unit) {
            final Integer stat = unitSpaceCount.get(unit);
            return stat != null && stat == 0;
        }
    }
}
