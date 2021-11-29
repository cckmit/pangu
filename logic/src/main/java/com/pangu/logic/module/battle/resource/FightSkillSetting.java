package com.pangu.logic.module.battle.resource;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.condition.ConditionType;
import com.pangu.framework.resource.Validate;
import com.pangu.framework.resource.anno.Id;
import com.pangu.framework.resource.anno.Resource;
import com.pangu.framework.utils.json.JsonUtils;
import com.pangu.framework.utils.lang.NumberUtils;
import lombok.Getter;
import lombok.Setter;

/**
 * 主动技能配置类
 */
@Resource("battle")
@Getter
@Setter
public class FightSkillSetting implements Validate {

    //  技能标识
    @Id
    private String id;

    // 技能标识(使用tag来标识一个技能，
    //因为一个技能有多个等级，所以用此tag来标记一类技能)
    private String tag;

    // 技能类型
    private SkillType type;

    //  技能施放的怒气要求
    private int anger;

    //  施放后可获得的怒气(扣减怒气为负数)
    private int mp;

    //  技能优先级
    private int priority;

    // 初始CD
    private int initCD;

    //  技能施放后的冷却时间
    private int coolTime;

    // 吟唱时间(抬手时间)
    private int singTime;

    // 吟唱结束后下一个动作开始延时
    private int singAfterDelay;

    // 大招暂停时间
    private int pauseTime;

    // 弹道单元飞行时间
    private int trackSpeed;

    // 循环执行次数
    private int executeTimes;

    // 首次执行延时
    private int firstTimeDelay;

    // 循环间隔
    private int executeInterval;

    // 循环间隔数组，优先取数组中的内容，没有
    private int[] intervals;

    //  技能的执行效果
    private String[] effects;

    //是否死亡时继续释放
    private boolean ignoreDie;

    // 技能触发条件
    private ConditionType conditionType;

    //  技能效果执行时所需的公式上下文内容
    private String conditionParam;

    // 真实
    private Object conditionRealParam;

    // 可替换的技能组
    private String[] upgrade;

    @Override
    public boolean isValid() {
        if (conditionType == null) {
            return true;
        }
        if (conditionRealParam != null) {
            return true;
        }
        Class<?> paramType = conditionType.getParamType();
        if (paramType == String.class) {
            conditionRealParam = conditionParam;
            return true;
        }
        if (paramType.isEnum()) {
            conditionRealParam = JsonUtils.convertObject(conditionParam, paramType);
            return true;
        }
        if (paramType.isPrimitive()) {
            conditionRealParam = NumberUtils.valueOf(paramType, conditionParam);
            return true;
        }
        conditionRealParam = JsonUtils.string2Object(conditionParam, paramType);
        return true;
    }

    /**
     * 与客户端相同的分隔符
     */
    final private static String SPLIT = "_LV";

    public static String getPrefix(String skillId) {
        return skillId.split("_LV")[0];
    }

    public static String toActiveSkillId(Unit unit, String prefix) {
        for (SkillState activeSkill : unit.getActiveSkills()) {
            final String id = activeSkill.getId();
            if (getPrefix(id).equals(prefix)) {
                return id;
            }
        }
        return null;
    }
}
