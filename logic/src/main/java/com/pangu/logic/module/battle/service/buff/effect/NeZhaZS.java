package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.BuffReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.service.buff.Buff;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.buff.param.NeZhaZSParam;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.param.StateAddParam;
import com.pangu.logic.module.battle.service.skill.utils.SkillUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 混天绫：当场上的火焰效果累积到3层之后，立刻恢复10%当前损失的生命值，一场战斗触发一次
 * 10：叠加到5层的时候，立刻恢复20%当前损失的生命值，一场战斗触发一次
 * 20：当有目标叠加到三层火焰时，使用混天绫将其禁锢住，持续3秒，被禁锢的目标受到的伤害提升50%，每个目标仅能被禁锢一次
 * 30：禁锢的时间提升至5秒
 */
@Component
public class NeZhaZS implements Buff {
    @Override
    public BuffType getType() {
        return BuffType.NEZHA_ZS;
    }

    @Override
    public void update(BuffState state, Unit unit, int time, Object addition) {
        //  获取一大波数据
        final NeZhaZSParam param = state.getParam(NeZhaZSParam.class);
        final String counterTag = param.getCounterTag();
        final int stateTriggerCount = param.getStateTriggerCount();
        final Addition triggerState = state.getAddition(Addition.class, new Addition());
        StateAddParam stateAddParam = param.getStateAddParam();
        UnitState unitState = null;
        int duration = 0;
        if (stateAddParam != null) {
            unitState = stateAddParam.getState();
            duration = stateAddParam.getTime();
        }
        final BuffReport buffReport = state.getBuffReport();

        //  根据单人火焰层数添加异常
        final Context context = new Context(unit);
        final List<Unit> enemies = unit.getEnemy().getCurrent();//此处不使用FilterType.ENEMY是为了在混乱状态下也能正确统计火焰印记层数
        int totalCount = 0;
        for (Unit enemy : enemies) {
            final BuffState counter = enemy.getBuffStateByTag(counterTag);
            if (counter == null) {
                continue;
            }
            final Integer count = counter.getAddition(Integer.class, 0);
            totalCount += count;

            if (unitState == null) {
                continue;
            }
            if (!enemy.canSelect(time)) {
                continue;
            }
            //  已添加过异常
            if (triggerState.single.contains(enemy)) {
                continue;
            }
            //  未达到释放条件
            if (count < stateTriggerCount) {
                continue;
            }
            //添加异常
            SkillUtils.addState(unit, enemy, unitState, time, duration + time, buffReport, context);
            //添加deBUFF
            BuffFactory.addBuff(param.getDeBuff(), unit, enemy, time, buffReport, null);
            //记录已影响过的目标
            triggerState.single.add(enemy);
        }

        //  根据场上火焰总层数回复生命
        final Map<Integer, Double> totalCount2CureRate = param.getTotalCount2CureRate();
        if (!CollectionUtils.isEmpty(totalCount2CureRate)) {
            for (Map.Entry<Integer, Double> entry : totalCount2CureRate.entrySet()) {
                final Integer conditionCount = entry.getKey();
                if (triggerState.total.contains(conditionCount)) {
                    continue;
                }
                if (totalCount < conditionCount) {
                    continue;
                }
                triggerState.total.add(conditionCount);
                final Double cureRate = entry.getValue();
                final long lossHp = unit.getValue(UnitValue.HP_MAX) - unit.getValue(UnitValue.HP);
                final long hpChange = (long) (lossHp * cureRate);
                context.addValue(unit, AlterType.HP, hpChange);
                buffReport.add(time, unit.getId(), Hp.of(hpChange));
            }
        }
        context.execute(time, buffReport);
    }

    private static class Addition {
        //  缓存专属触发情况
        private Set<Integer> total = new HashSet<>(2);
        private Set<Unit> single = new HashSet<>(6);
    }
}
