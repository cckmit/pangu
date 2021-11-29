package com.pangu.logic.module.battle.service.core;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.resource.FightSkillSetting;
import com.pangu.logic.module.battle.service.skill.condition.ConditionType;
import com.pangu.framework.resource.JsonObject;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

/**
 * 技能状态
 */
@Getter
@Setter
public class SkillState implements Cloneable, JsonObject {

    //  技能标识
    private final FightSkillSetting setting;

    //  技能效果状态
    private final List<EffectState> effectStates;

    // 攻击距离(根据技能效果目标选择的最小距离为此距离)
    private final int range;

    // 持有原始技能的引用，以便计算替换技能cd时同步原始技能cd
    private SkillState originSkillState;

    //  当前冷却
    private int cd;

    //  缓存当前技能在一场战斗中所需的额外数据
    private Object addition;
    //  缓存当前技能在一场战斗中执行过几次
    private int loops;

    /**
     * CD共享的技能
     */
    private SkillState cdShareSkillState;

    public SkillState(FightSkillSetting setting, List<EffectState> effectStates, int range) {
        this.setting = setting;
        this.effectStates = effectStates;
        this.range = range;
        this.cd = setting.getInitCD();
    }

    /**
     * 检查战斗单位能否选择该技能
     *
     * @param time
     * @param unit
     * @return
     */
    public boolean isValid(int time, Unit unit) {
        SkillType type = setting.getType();
        // 如果是大招，判断怒气是否足够

        if (type == SkillType.NORMAL) {
            // 麻痹状态无法释放普攻
            if (unit.hasState(UnitState.PALSY, time)) {
                return false;
            }
        } else {
            // 沉默状态无法释放大招和技能
            if (unit.hasState(UnitState.CHAOS, time) || unit.hasState(UnitState.SILENT, time)) {
                return false;
            }
            // 如果是大招，监测怒气是否足够
            if (type == SkillType.SPACE) {
                // 封印状态时，不能释放大招
                if (unit.hasState(UnitState.FENG_YING, time)) {
                    return false;
                }
                return unit.getValue(UnitValue.MP) >= setting.getAnger() && (cd - time) <= 0;
            }
        }
        // 普攻和技能监测CD时间
        return (cd - time) <= 0;
    }

    public void setCd(int time) {
        cd = time;
        if (originSkillState != null) {
            originSkillState.setCd(time);
        }
        if (cdShareSkillState != null) {
            cdShareSkillState.cd = cd;
        }
    }

    public void calCd(int time, int singAfterDelay) {
        int afterTime = Math.max(setting.getCoolTime() + singAfterDelay, 50);
        cd = time + afterTime;
        if (originSkillState != null) {
            originSkillState.calCd(time, singAfterDelay);
        }
        if (cdShareSkillState != null) {
            cdShareSkillState.cd = cd;
        }
    }

    public boolean shareCdWith(SkillState skillState) {
        if (cdShareSkillState != null || skillState.cdShareSkillState != null) {
            return false;
        }
        if (skillState == this) {
            return false;
        }
        cdShareSkillState = skillState;
        skillState.cdShareSkillState = this;
        return true;
    }

    public SkillType getType() {
        return setting.getType();
    }

    public int getPriority() {
        return setting.getPriority();
    }

    public String getId() {
        return setting.getId();
    }

    public int getSingTime() {
        return setting.getSingTime();
    }

    public int getMp() {
        return setting.getMp();
    }

    public int getAnger() {
        return setting.getAnger();
    }

    public int getCoolTime() {
        return setting.getCoolTime();
    }

    public boolean isTrack() {
        return setting.getTrackSpeed() > 0;
    }

    public int getTrackSpeed() {
        return setting.getTrackSpeed();
    }

    public int getSingAfterDelay() {
        return setting.getSingAfterDelay();
    }

    public int getFirstTimeDelay() {
        return setting.getFirstTimeDelay();
    }

    public int getExecuteTimes() {
        return setting.getExecuteTimes();
    }

    public int getExecuteInterval() {
        return setting.getExecuteInterval();
    }

    public int getExecuteInterval(int execTimes) {
        final int[] intervals = setting.getIntervals();
        if (ArrayUtils.isEmpty(intervals)) {
            return setting.getExecuteInterval();
        }
        if (intervals.length <= execTimes - 1) {
            return setting.getExecuteInterval();
        }
        return intervals[execTimes - 1];
    }

    public int getPauseTime() {
        return setting.getPauseTime();
    }

    public String getTag() {
        String tag = setting.getTag();
        if (tag == null) {
            return setting.getId();
        }
        return tag;
    }

    public boolean conditionValid(Unit owner, int time) {
        ConditionType conditionType = setting.getConditionType();
        if (conditionType == null) {
            return true;
        }
        return conditionType.valid(this, owner, time, setting.getConditionRealParam());
    }

    public void calLoops() {
        loops++;
    }

    public <T> T getAddition(Class<T> clz) {
        //noinspection unchecked
        return (T) addition;
    }
}
