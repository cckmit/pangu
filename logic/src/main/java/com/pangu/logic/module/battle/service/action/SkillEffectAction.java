package com.pangu.logic.module.battle.service.action;

import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.*;
import com.pangu.logic.module.battle.service.passive.PassiveFactory;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillReleasePassive;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 技能执行Action
 */
@Getter
public class SkillEffectAction extends CloseableAction {

    private int time;

    private final Unit owner;

    private final SkillState skillState;

    private final SkillReport skillReport;

    // 如果是弹道类，是否已经执行过追击
    private boolean fly;

    // 首次执行延迟
    private boolean firstDelay;

    // 已经执行次数
    private int executeTimes;

    // 需要执行的总次数，定义在此处便于某些效果在本对象生命周期内动态修改总执行次数，而不影响到静态资源对象
    @Setter
    private int totalExecTimes;

    // 弹道类技能会进行一次目标选择
    private List<Unit> targets;

    // 记录技能的多次循环间共享的数据
    @Setter
    private Object addition;

    public SkillEffectAction(int time, Unit owner, SkillState skillState, SkillReport skillReport) {
        this.time = time;
        this.owner = owner;
        this.skillState = skillState;
        this.totalExecTimes = skillState.getExecuteTimes();
        this.skillReport = skillReport;
    }

    @Override
    public void execute() {
        if (!fly && skillState.isTrack()) {
            executeTrack();
            return;
        }

        /* 已结束 */
        if (isDone()) {
            return;
        }

        // 首次效果延时
        if (executeTimes == 0 && !firstDelay && skillState.getFirstTimeDelay() > 0) {
            firstDelay = true;
            time += skillState.getFirstTimeDelay();
            if (skillState.getSetting().isIgnoreDie()) {
                owner.getBattle().addWorldAction(this);
            } else {
                owner.addTimedAction(this);
            }
            return;
        }

        ++executeTimes;
        //将执行次数同步到当前SkillState
        skillState.calLoops();

        // 执行技能效果
        executeEffect();
        if (executeTimes >= this.totalExecTimes) {
            return;
        }

        time += skillState.getExecuteInterval(executeTimes);
        if (skillState.getSetting().isIgnoreDie()) {
            owner.getBattle().addWorldAction(this);
        } else {
            owner.addTimedAction(this);
        }
    }

    private void executeEffect() {
        List<EffectState> effectStates = skillState.getEffectStates();
        if (effectStates == null || effectStates.isEmpty()) {
            return;
        }
        List<Unit> targets = this.targets;
        String preTargetId = effectStates.get(0).getTarget();
        if (targets == null) {
            targets = TargetSelector.select(owner, preTargetId, time);
        }
        Context context = new Context(owner);
        context.setLoopTimes(executeTimes);
        context.setRootSkillEffectAction(this);
        context.setExecPassive(true);
        // 执行技能释放被动，保证被动只执行一次
        if (executeTimes == 1) {
            Fighter fighterOne = owner.getFriend();
            Fighter fighterTwo = owner.getEnemy();
            executeSkillReleasePassive(time, skillReport, fighterOne, owner, skillState, context);
            executeSkillReleasePassive(time, skillReport, fighterTwo, owner, skillState, context);
        }

        for (EffectState effectState : effectStates) {
            if (!effectState.getTarget().equals(preTargetId)) {
                preTargetId = effectState.getTarget();
                targets = TargetSelector.select(owner, preTargetId, time);
            }
            if (targets.isEmpty()) {
                continue;
            }
            if (effectState.getDelay() > 0) {
                EffectAction effectAction = new EffectAction(time + effectState.getDelay(), owner, skillState, skillReport, effectState, targets);
                effectAction.setRootSkillEffectAction(this);
                if (skillState.getSetting().isIgnoreDie()) {
                    owner.getBattle().addWorldAction(effectAction);
                } else {
                    owner.addTimedAction(effectAction);
                }
                continue;
            }
            if (effectState.isDynamicPlayTime()) {
                for (Unit target : targets) {
                    final int dist = target.calcDistance(owner);
                    final int flyTime = getFlyTime(dist, skillState.getTrackSpeed());
                    EffectAction effectAction = new EffectAction(time + flyTime, owner, skillState, skillReport, effectState, Collections.singletonList(target));
                    effectAction.setRootSkillEffectAction(this);
                    if (skillState.getSetting().isIgnoreDie()) {
                        owner.getBattle().addWorldAction(effectAction);
                    } else {
                        owner.addTimedAction(effectAction);
                    }
                }
                continue;
            }

            SkillEffect skillEffect = SkillFactory.getSkillEffect(effectState.getId());
            context.setTargetAmount(targets.size());
            for (Unit target : targets) {
                context.execAttackBeforePassiveAndEffect(time, skillEffect, target, skillState, effectState, skillReport);
            }
        }

        // 属性生效
        context.execute(time, skillState, skillReport);
    }

    private void executeTrack() {
        List<EffectState> effectStates = skillState.getEffectStates();
        if (effectStates.isEmpty()) {
            return;
        }
        String targetId = effectStates.get(0).getTarget();
        this.targets = TargetSelector.select(owner, targetId, time);
        if (targets.isEmpty()) {
            return;
        }
        Point point = owner.getPoint();
        int distanceSum = 0;
        for (Unit target : targets) {
            distanceSum += point.distance(target.getPoint());
        }
        int average = distanceSum / targets.size();

        // 执行飞行时间后才结算技能效果
        int flyTime = getFlyTime(average, skillState.getTrackSpeed());
        time += flyTime;

        fly = true;

        // 重新进入计时器
        if (skillState.getSetting().isIgnoreDie()) {
            owner.getBattle().addWorldAction(this);
        } else {
            owner.addTimedAction(this);
        }
    }

    public static int getFlyTime(int dist, int trackSpeed) {
        return 100 * dist / trackSpeed;
    }

    private void executeSkillReleasePassive(int time, SkillReport skillReport, Fighter fighter, Unit attacker, SkillState skillState, Context context) {
        Set<Unit> units = fighter.getSkillListener();
        if (units == null || units.size() == 0) {
            return;
        }
        for (Unit curUnit : units.toArray(new Unit[0])) {
            List<PassiveState> passiveStates = curUnit.getSkillPassive();
            if (passiveStates == null || passiveStates.size() == 0) {
                continue;
            }
            for (PassiveState passiveState : passiveStates.toArray(new PassiveState[0])) {
                if (passiveState.invalid(time)) {
                    continue;
                }
                PassiveType passiveType = passiveState.getType();
                SkillReleasePassive skillPassive = PassiveFactory.getPassive(passiveType);
                // 执行被动
                skillPassive.skillRelease(passiveState, curUnit, attacker, skillState, time, context, skillReport);
                if (passiveState.shouldRemove(time)) {
                    passiveStates.remove(passiveState);
                    if (passiveStates.size() != 0) {
                        continue;
                    }
                    units.remove(curUnit);
                }
            }
        }
    }

    public <T> T getAddition(Class<T> clz) {
        //noinspection unchecked
        return (T) addition;
    }

    public <T> T getAddition(Class<T> clz, T def) {
        if (addition == null) {
            addition = def;
        }
        //noinspection unchecked
        return (T) addition;
    }

    public void addTotalExecTimes(int add) {
        totalExecTimes += add;
    }
}
