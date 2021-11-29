package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.DieUnitReduceDamageParam;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 指定死亡单元死亡后降低自己受到的伤害
 */
@Component
public class DieUnitReduceDamage implements DamagePassive {

    @Override
    public PassiveType getType() {
        return PassiveType.DAMAGE_REDUCTION;
    }

    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final long value = owner.getValue(UnitValue.HP);
        if (value >= 0) {
            return;
        }
        final DieUnitReduceDamageParam param = passiveState.getParam(DieUnitReduceDamageParam.class);
        List<Unit> units = passiveState.getAddition(List.class);
        if (units == null) {//初始化 需要死亡的单元
            final List<Integer> sequences = param.getSequence();
            if (sequences == null) {
                return;
            }
            units = new ArrayList<>(sequences.size());
            for (Unit unit : owner.getFriend().getCurrent()) {
                if (sequences.contains(unit.getSequence())) {
                    units.add(unit);
                }
            }
            passiveState.setAddition(units);
        }
        if (units.isEmpty()) {
            return;
        }
        if (param.isNeedDie()) {//需要全部死亡才触发
            for (Unit unit : units) {
                if (!unit.isDead()) {
                    return;
                }
            }
        } else {//有一个存活就能触发
            int aliveNumbers = 0;
            for (Unit unit : units) {
                if (!unit.isDead()) {
                    aliveNumbers++;
                    break;
                }
            }
            if (aliveNumbers == 0) {
                return;
            }
        }
        final double decreaseRate = param.getRate();
        long increaseHp = (long) (-decreaseRate * value);

        PassiveUtils.hpUpdate(context,skillReport,owner,increaseHp,time);

        passiveState.addCD(time);
    }
}
