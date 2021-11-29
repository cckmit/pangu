package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.report.BuffReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.model.report.values.StateRemove;
import com.pangu.logic.module.battle.service.buff.Buff;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.utils.SkillUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * 蝴蝶仙子·莉亚娜技能：迷蝶花海
 * 1级：在空中来回飞舞一次,撒下花粉,使敌方全体陷入睡眠,持续3秒睡眠结束时会造成睡眠时受到伤害的25%的伤害
 * 2级：持续时间提升至4秒
 * 3级：睡眠结束时伤害提升至睡眠时伤害的30%
 * 4级：持续时间提升至5秒
 *
 * @author Kubby
 */
@Component
public class MiDieHuaHaiBuff implements Buff {

    @Override
    public BuffType getType() {
        return BuffType.MI_DIE_HUA_HAI;
    }

    @Override
    public boolean add(BuffState state, Unit unit, int time) {
        UnitState type = UnitState.DISABLE;
        if (type.immune != null && type.immune.length != 0) {
            for (UnitState immu : type.immune) {
                if (unit.hasState(immu, time)) {
                    return false;
                }
            }
        }

        unit.addBuff(state);

        BuffReport buffReport = state.getBuffReport();
        final Unit caster = state.getCaster();
        Context context = new Context(caster);
        SkillUtils.addState(caster, unit, type, time, time + state.getTime(), buffReport, context);
        context.execute(time, buffReport);

        return true;
    }

    @Override
    public void update(BuffState state, Unit unit, int time, Object addition) {

    }

    @Override
    public void remove(BuffState state, Unit unit, int time) {
        unit.removeBuff(state);
        MiDieHuaHaiBuffAddition addition = state.getAddition(MiDieHuaHaiBuffAddition.class);
        long damage = addition.getFinalDamage();
        Context context = new Context(state.getCaster());
        BuffReport buffReport = state.getBuffReport();
        context.addValue(unit, AlterType.HP, damage);
        buffReport.add(time, unit.getId(), Hp.of(damage));
        context.execute(time, buffReport);
        final boolean success = unit.removeState(UnitState.DISABLE, time);
        if (success) {
            buffReport.add(time, unit.getId(), new StateRemove(Collections.singletonList(UnitState.DISABLE)));
        }
        addition.clear();
    }

    public static class MiDieHuaHaiBuffAddition {

        private long damage;

        private double rate;

        public void incDamage(long v) {
            damage += v;
        }

        public void clear() {
            damage = 0;
        }

        public long getFinalDamage() {
            return (long) (damage * rate);
        }

        public static MiDieHuaHaiBuffAddition of(double rate) {
            MiDieHuaHaiBuffAddition addition = new MiDieHuaHaiBuffAddition();
            addition.rate = rate;
            return addition;
        }
    }
}
