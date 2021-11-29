package com.pangu.logic.module.battle.service.skill.condition;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveType;

/**
 * 判断类型
 */
public enum ConditionType {

    // 身上不存在某个buff才能触发
    NO_BUF_TAG(new NoBuff()),

    // 没有目标时才释放
    NO_TARGET(new NoTarget()),

    // 身上有指定被动类型的被动
    HAS_PASSIVE_TYPE(PassiveType.class, new HasPassiveType()),

    /** 自身血量低于百分比时 */
    HP_PCT_LOWER(Double.class, new HpPctLower()),

    /** 战斗开始一段时间后  */
    TIME_LAPSE(Integer.class, new TimeLapse()),

    /** 技能类型  */
    IS_SKILL_TYPE(SkillType.class, new IsSkillType()),

    /** 表达式 */
    EXPR(new Expr()),

    ;

    private final Class<?> paramType;

    private final SkillReleaseCondition condition;

    ConditionType(SkillReleaseCondition condition) {
        this.paramType = String.class;
        this.condition = condition;
    }

    ConditionType(Class<?> paramType, SkillReleaseCondition condition) {
        this.paramType = paramType;
        this.condition = condition;
    }

    public Class<?> getParamType() {
        return paramType;
    }


    public boolean valid(SkillState skillState, Unit unit, int time, Object param){
        return condition.valid(skillState, unit, time, param);
    }
}
