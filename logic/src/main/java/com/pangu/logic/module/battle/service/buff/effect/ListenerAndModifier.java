package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.BuffReport;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.buff.Buff;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.buff.param.DefaultAddValueParam;
import com.pangu.logic.module.battle.service.buff.param.ListenerAndModifierParam;
import com.pangu.logic.module.battle.service.buff.utils.CalTypeHelper;
import com.pangu.logic.module.battle.service.buff.utils.CalValues;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.skill.param.StateAddParam;
import com.pangu.logic.module.battle.service.skill.utils.SkillUtils;
import com.pangu.logic.utils.ExpressionHelper;
import com.pangu.framework.utils.reflect.StringUtils;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 监听指定单元的状态并修改属性。
 */
@Component
public class ListenerAndModifier implements Buff {
    @Override
    public BuffType getType() {
        return BuffType.LISTENER_AND_MODIFIER;
    }

    @Override
    public void update(BuffState state, Unit unit, int time, Object addition) {
        final ListenerAndModifierParam param = state.getParam(ListenerAndModifierParam.class);

        final List<Unit> targets;
        final String listeningTarget = param.getListeningTarget();
        if (!StringUtils.isEmpty(listeningTarget)) {
            targets = TargetSelector.select(unit, listeningTarget, time);
        } else if (param.isFriend()) {
            targets = unit.getFriend().getCurrent();
        } else {
            targets = unit.getEnemy().getCurrent();
        }

        Context context = new Context(unit);
        final BuffReport buffReport = state.getBuffReport();
        final DefaultAddValueParam valModParam = param.getValModParam();

        final ConditionContext conditionContext = new ConditionContext(unit, targets, time);
        ExpressionHelper.invoke(param.getFilterExp(), void.class, conditionContext);

        switch (param.getStrategy()) {
            case ONCE_RECOVERABLE: {
                final Addition stateAddition = state.getAddition(Addition.class, new Addition());
                final Set<Unit> modUnits = stateAddition.modUnits;
                for (Unit passer : conditionContext.pass) {
                    if (modUnits.contains(passer)) {
                        continue;
                    }
                    modUnits.add(passer);

                    final CalValues calValues = CalTypeHelper.calValues(valModParam.getCalType(), valModParam.getAlters(), unit, passer, valModParam.getFactor());
                    final String passerId = passer.getId();
                    for (Map.Entry<AlterType, Number> entry : calValues.getValues().entrySet()) {
                        final AlterType alterType = entry.getKey();
                        Number valueChange = entry.getValue();
                        context.addValue(passer, alterType, valueChange);
                        buffReport.add(time, passerId, new UnitValues(alterType, valueChange));
                    }
                }
                for (Unit failure : conditionContext.fail) {
                    if (!modUnits.contains(failure)) {
                        continue;
                    }
                    modUnits.remove(failure);

                    final CalValues calValues = CalTypeHelper.calValues(valModParam.getCalType(), valModParam.getAlters(), unit, failure, valModParam.getFactor());
                    final String failureId = failure.getId();
                    for (Map.Entry<AlterType, Number> entry : calValues.getValues().entrySet()) {
                        final AlterType alterType = entry.getKey();
                        Number valueChange = -entry.getValue().doubleValue();
                        context.addValue(failure, alterType, valueChange);
                        buffReport.add(time, failureId, new UnitValues(alterType, valueChange));
                    }
                }
                break;
            }
            case REPEAT_UNRECOVERABLE: {
                for (Unit passer : conditionContext.pass) {
                    final CalValues calValues = CalTypeHelper.calValues(valModParam.getCalType(), valModParam.getAlters(), unit, passer, valModParam.getFactor());
                    for (Map.Entry<AlterType, Number> entry : calValues.getValues().entrySet()) {
                        final AlterType alterType = entry.getKey();
                        Number valueChange = entry.getValue();
                        context.addValue(passer, alterType, valueChange);
                        buffReport.add(time, passer.getId(), new UnitValues(alterType, valueChange));
                    }
                }
                break;
            }
            case STATE_ADD_UNRECOVERABLE: {
                final StateAddParam stateAddParam = param.getStateAddParam();
                if (stateAddParam == null) {
                    return;
                }
                if (param.isNeedStateReport()) {
                    for (Unit passer : conditionContext.pass) {
                        SkillUtils.addState(unit, passer, stateAddParam.getState(), time, stateAddParam.getTime() + time, buffReport, context);
                    }
                } else {
                    for (Unit passer : conditionContext.pass) {
                        SkillUtils.addState(unit, passer, stateAddParam.getState(), time, stateAddParam.getTime() + time, null, context);
                    }
                }
            }
        }

        context.execute(time, buffReport);
    }

    @Getter
    public class ConditionContext {
        private Unit buffOwner;
        private int time;
        private List<Unit> pass = new ArrayList<>(6);
        private List<Unit> fail = new ArrayList<>(6);

        public ConditionContext(Unit unit, List<Unit> targets, int time) {
            this.buffOwner = unit;
            this.pass.addAll(targets);
            this.time = time;
        }

        /**
         * 以生命值百分比为阈值进行分组
         */
        public void hpPctLessThan(double threshold) {
            final List<Unit> newPassers = new ArrayList<>(6);
            for (Unit unit : pass) {
                if (unit.getHpPct() < threshold) {
                    newPassers.add(unit);
                } else {
                    fail.add(unit);
                }
            }
            pass = newPassers;
        }

        public void hpPctMoreThan(double threshold) {
            final List<Unit> newPassers = new ArrayList<>(6);
            for (Unit unit : pass) {
                if (unit.getHpPct() >= threshold) {
                    newPassers.add(unit);
                } else {
                    fail.add(unit);
                }
            }
            pass = newPassers;
        }

        /**
         * 以前排是否全部阵亡进行分组
         */
        public void frontLiving() {
            int livingFront = 0;
            for (Unit unit : pass) {
                if (unit.isDead()) {
                    continue;
                }
                if (unit.getSequence() == 0 || unit.getSequence() == 1) {
                    livingFront++;
                }
            }

            if (livingFront > 0) {
                return;
            }

            fail.addAll(pass);
            pass.clear();
        }

        /**
         * 以我方存活英雄数量是否少于某个临界值进行分组
         */
        public void livingHeroCountLessThan(int threshold) {
            int livingHero = 0;
            for (Unit unit : pass) {
                if (unit.isDead()) {
                    continue;
                }
                if (unit.isInitByBattle()) {
                    livingHero++;
                }
            }

            if (livingHero < threshold) {
                return;
            }

            fail.addAll(pass);
            pass.clear();
        }

        /**
         * 以是否为后排进行分组
         */
        public void backRow() {
            final List<Unit> newPassers = new ArrayList<>(6);
            for (Unit unit : pass) {
                if (unit.getSequence() > 1) {
                    newPassers.add(unit);
                } else {
                    fail.add(unit);
                }
            }
            pass = newPassers;
        }

        /**
         * 以是否处于异常状态进行分组
         */
        public void inHarmfulState() {
            final List<Unit> newPassers = new ArrayList<>(6);
            for (Unit unit : pass) {
                if (unit.inHarmfulState(time)) {
                    newPassers.add(unit);
                } else {
                    fail.add(unit);
                }
            }
            pass = newPassers;
        }

        /**
         * 以是否被控制进行分组
         */
        public void underControl() {
            final List<Unit> newPassers = new ArrayList<>(6);
            for (Unit unit : pass) {
                if (unit.underControl(time)) {
                    newPassers.add(unit);
                } else {
                    fail.add(unit);
                }
            }
            pass = newPassers;
        }
    }

    private class Addition {
        private Set<Unit> modUnits = new HashSet<>(6);
    }
}
