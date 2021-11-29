package com.pangu.logic.module.battle.service.action;

import com.pangu.logic.module.battle.model.*;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.Battle;
import com.pangu.logic.module.battle.service.BattleConstant;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.Fighter;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveFactory;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillSelectPassive;
import com.pangu.logic.module.battle.service.select.RangeHelper;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.List;

/**
 * 生成出手行动的行动
 * <pre>
 * 该行动对象负责出手速度的频率控制
 * 不进行战报的输出
 * </pre>
 */
@Slf4j
@Getter
public class BeginAction implements Action {

    private int time;

    private final Unit owner;

    public BeginAction(int time, Unit owner) {
        this.time = time;
        this.owner = owner;
    }

    @Override
    public void execute() {
        // 优先执行羁绊行动
        SkillState fatterSkill = owner.takeFatterSkill();
        if (fatterSkill != null) {
            executeSkill(fatterSkill);
            return;
        }
        // 优先执行初始化技能
        SkillState initSkill = owner.takeInitSkill();
        if (initSkill != null) {
            executeSkill(initSkill);
            return;
        }
        // 开始执行寻路以及技能
        // 如果已经死亡则终止行动
        if (owner.isDead()) {
            if (log.isDebugEnabled()) {
                log.debug("单位[{}]死亡...[{}]", owner.getId(), time);
            }
            return;
        }

        // 进入吟唱
        SkillState activeSkill = selectSkill(owner);
        if (activeSkill == null) {
            if (!canOwnerSelectSkill()) {
                time = time + 50;
                owner.updateAction(this);
                return;
            }
            activeSkill = owner.getNormalSkill();
            if (activeSkill == null) {
                time = time + 50;
                owner.updateAction(this);
                return;
            }
            SkillState replaceSkill = executeSkillSelectPassive(activeSkill);
            if (replaceSkill != activeSkill) {
                int replaceSkillPreCoolTime = this.time + replaceSkill.getSingAfterDelay() + replaceSkill.getCoolTime()
                        - activeSkill.getSingAfterDelay() - activeSkill.getCoolTime();
                if (!replaceSkill.isValid(replaceSkillPreCoolTime, owner)) {
                    time = time + 50;
                    owner.updateAction(this);
                    return;
                }
                activeSkill = replaceSkill;
            }
            if (!activeSkill.isValid(time, owner)) {
                time = time + 50;
                owner.updateAction(this);
                return;
            }

            if (activeSkill.getRange() > 0) {
                owner.setMinMoveDistance(activeSkill.getRange());
            }
        } else {
            activeSkill = executeSkillSelectPassive(activeSkill);
        }
        if (activeSkill.getRange() == 0) {
            Unit target = RangeHelper.getTarget(owner, false, time);
            owner.setTarget(target);
            executeSkill(activeSkill);
            return;
        }
        Unit target = RangeHelper.getTarget(owner, true, time);
        owner.setTarget(target);
        if (target == null) {
            if (owner.hasState(UnitState.FOLLOW, time)) {
                target = RangeHelper.getTarget(owner, false, time);
                if (target == null || target.getFriend() != owner.getFriend()) {
                    time = time + 50;
                    owner.updateAction(this);
                    return;
                }
            }
            long speed = owner.getValue(UnitValue.SPEED);
            int moveIntervalMill = BattleConstant.MoveIntervalMill;
            if (speed > 0) {
                Action move = new MoveAction(time + moveIntervalMill, owner);
                owner.updateAction(move);
            } else {
                // 无法移动的行动单元，可能只能释放技能，暂定行动间隔200毫秒
                time += 200;
                owner.updateAction(this);
            }
            return;
        }
        // 执行技能
        executeSkill(activeSkill);
    }

    private boolean canOwnerSelectSkill() {
        if (owner.hasState(UnitState.DISABLE, time)) {
            return false;
        }
        if (owner.hasState(UnitState.EXILE, time)) {
            return false;
        }
        if (owner.hasState(UnitState.FROZEN, time)) {
            return false;
        }
        return true;
    }

    private SkillState executeSkillSelectPassive(SkillState activeSkill) {
        List<PassiveState> passiveStates = owner.getSkillSelectPassive();
        if (passiveStates == null || passiveStates.size() == 0) {
            return activeSkill;
        }
        SkillState replaceSkill = executeSkillSelectPassive(activeSkill, owner, time, passiveStates);
        if (replaceSkill != null && replaceSkill != activeSkill) {
            //将替换技能与原始技能进行绑定，便于SkillAction计算替换的cd时，反馈到原始技能上，使得选择技能能够正常执行
            replaceSkill.setOriginSkillState(activeSkill);
            return replaceSkill;
        }
        return activeSkill;
    }

    private SkillState selectSkill(Unit owner) {
        if (!canOwnerSelectSkill()) {
            return null;
        }
        List<SkillState> skillStates = owner.getActiveSkills();
        Battle battle = owner.getBattle();
        for (SkillState skillState : skillStates) {
            if (!skillState.isValid(time, owner)) {
                continue;
            }
            List<EffectState> effectStates = skillState.getEffectStates();
            if (effectStates == null || effectStates.isEmpty()) {
                continue;
            }
            if (!skillState.conditionValid(owner, time)) {
                continue;
            }
            List<Unit> select = TargetSelector.select(owner, effectStates.get(0).getTarget(), time);
            if (select.isEmpty()) {
                continue;
            }
            if (skillState.getType() == SkillType.SPACE) {
                if (battle.isSpaceCD(time)) {
                    battle.addSpaceValidUnit(owner);
                    continue;
                }
            }
            return skillState;
        }
        return null;
    }

    private SkillState executeSkillSelectPassive(SkillState skillState, Unit owner, int time, List<PassiveState> passiveStates) {
        Iterator<PassiveState> iterator = passiveStates.iterator();
        while (iterator.hasNext()) {
            PassiveState passiveState = iterator.next();
            if (passiveState.invalid(time)) {
                continue;
            }
            PassiveType passiveType = passiveState.getType();
            SkillSelectPassive damagePassive = PassiveFactory.getPassive(passiveType);
            // 执行被动
            SkillState replaceSkill = damagePassive.skillSelect(passiveState, skillState, owner, time);
            if (passiveState.shouldRemove(time)) {
                iterator.remove();
            }
            if (replaceSkill != null) {
                return replaceSkill;
            }
        }
        return null;
    }

    private void executeSkill(SkillState skillState) {
        Battle battle = owner.getBattle();
        if (skillState.getType() == SkillType.SPACE) {
            battle.addSpaceCD(time);
            // 大招CD后，提前2ms进行大招检测
            battle.addWorldAction(new SpaceTimeLineAction(battle.getSpaceCD(), battle));
        }
        // 吟唱
        int singTime = skillState.getSingTime();
        int singAfterDelay = skillState.getSingAfterDelay();
        if (skillState.getType() == SkillType.NORMAL) {
            double rate = owner.getRate(UnitRate.NORMAL_SKILL_UP) - owner.getRate(UnitRate.NORMAL_SKILL_DOWN);
            rate = Math.max(-0.9, rate);
            rate = 1 / (1 + rate);
            // 防止吟唱时间变为0
            singTime = (int) (singTime * Math.max(0.1, rate));
            singAfterDelay = (int) (singAfterDelay * Math.max(0.1, rate));
        } else if (skillState.getType() == SkillType.SPACE) {
            BattleType type = battle.getType();
            Fighter friend = owner.getFriend();
            if (!owner.getBattle().isPause() && friend != null && !friend.isAttacker()) {
                singTime += skillState.getPauseTime();
            }
        }
        // 添加保存技能战报
        Unit target = owner.getTarget();
        SkillReport skillReport = SkillReport.sing(time, owner.getId(), skillState.getId(), singTime, target != null && !target.isDead() ? target.getId() : null);
        battle.addReport(skillReport);

        SkillAction skillAction = new SkillAction(time + singTime, owner, skillState, skillReport, singAfterDelay);
        owner.updateAction(skillAction);
        if (singTime != 0) {
            return;
        }
        skillAction.execute();
    }
}
