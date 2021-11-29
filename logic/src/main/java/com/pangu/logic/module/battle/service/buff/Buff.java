package com.pangu.logic.module.battle.service.buff;

import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.utils.ExpressionHelper;
import com.pangu.framework.utils.reflect.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * buff效果
 */
public interface Buff {

    /**
     * buff类型
     *
     * @return
     */
    BuffType getType();

    /**
     * 增加Buff
     *
     * @param state
     * @param unit
     * @param time
     * @return 是否添加成功
     */
    default boolean add(BuffState state, Unit unit, int time) {
        unit.addBuff(state);
        return true;
    }

    ;

    /**
     * 由定时器（buff自身）或外部（其他buff、被动、技能）触发
     *
     * @param state
     * @param unit
     * @param time
     * @param addition
     */
    void update(BuffState state, Unit unit, int time, Object addition);

    /**
     * 移除Buff
     *
     * @param state
     * @param unit
     * @param time
     * @return
     */
    default void remove(BuffState state, Unit unit, int time) {
        unit.removeBuff(state);
    }

    /**
     * 执行条件校验，不耦合进基本接口里，buff实现仅在需要时调用，尽可能减少性能损耗
     *
     * @param state
     * @param unit
     * @param condExpr
     * @param time
     * @return true:通过校验
     */
    default boolean condVerify(BuffState state, Unit unit, String condExpr, int time) {
        if (!StringUtils.isEmpty(condExpr)) {
            final Map<String, Object> ctx = new HashMap<>(4, 1);
            ctx.put("state", state);
            ctx.put("owner", state.getCaster());
            ctx.put("target", unit);
            ctx.put("time", time);
            if (!ExpressionHelper.invoke(condExpr, boolean.class, ctx)) {
                return false;
            }
        }
        return true;
    }
}
