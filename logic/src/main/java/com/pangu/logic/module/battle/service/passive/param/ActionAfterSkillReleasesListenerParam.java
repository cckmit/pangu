package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.service.skill.param.StateAddParam;
import lombok.Getter;

@Getter
public class ActionAfterSkillReleasesListenerParam {
    /**
     * 触发更新技能行为的技能释放次数阈值
     */
    private int triggerCount;
    /**
     * 可纳入统计的技能类型
     */
    private SkillType[] skillTypes;
    /**
     * 可纳入统计的的技能释放阵营
     */
    private boolean friendInclude;
    private boolean enemyInclude;

    /**
     * 达到触发次数后释放的技能
     */
    private String skillId;

    /**
     * 达到触发次数后添加的异常状态
     */
    private StateAddParam stateAddParam;
    /**
     * 被添加异常状态的目标
     */
    private String stateTarget;

    /**
     * 达到触发次数后采取的行为
     */
    private ActionType actionType = ActionType.SKILL_UPDATE;

    public enum ActionType {
        STATE_ADD, SKILL_UPDATE
    }
}
