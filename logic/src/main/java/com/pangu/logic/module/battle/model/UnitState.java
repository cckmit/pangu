package com.pangu.logic.module.battle.model;

import com.pangu.framework.protocol.annotation.Transable;

import java.util.HashSet;
import java.util.Set;

/**
 * 战斗单位的状态定义
 */
@Transable
public enum UnitState {
    /**
     * 变身封印
     */
    TRANSFORM_SEAL(false),
    /**
     * 被动封印
     */
    PASSIVE_SEAL(true),

    /**
     * 锁定，通常仅用于生成战报，供前端锚定目标
     */
    AIM(false),

    /**
     * 霸体(33554432)
     */
    BA_TI(false),

    /**
     * 隐身/无法被选择(4)
     */
    UNVISUAL(false),

    /**
     * 不可移动，相当于定住
     */
    NO_MOVE(true, UnitState.BA_TI),


    /**
     * 不可闪避
     */
    NO_DODGE(true),

    /**
     * 免疫死亡(1)
     */
    IMMUNE_DEA(false),

    /**
     * 免疫:物理免疫(256)
     */
    IMMUNE_PHYSICS_IMMUNE(false),

    /**
     * 免疫:禁止行动(16)
     */
    IMMUNE_DISABLE(false),
    /**
     * 免疫:冻结
     */
    IMMUNE_FROZEN(false),
    /**
     * 免疫:混乱/反选主动技能目标(64)
     */
    IMMUNE_CHAOS(false),
    /**
     * 免疫:沉默/禁技(4096)
     */
    IMMUNE_SILENT(false),
    /**
     * 嘲讽免疫(16384)
     */
    IMMUNE_SNEER(false),
    /**
     * 麻痹免疫(65536)
     */
    IMMUNE_PALSY(false),
    /**
     * 迷乱免疫(262144)
     */
    IMMUNE_CONFUSION(false),
    /**
     * 放逐免疫(1048576)
     */
    IMMUNE_EXILE(false),
    /**
     * 缠绕免疫(4194304)
     */
    IMMUNE_BIND(false),

    /**
     * 封印免疫
     */
    IMMUNE_FENG_YING(false),

    /**
     * 封印(是不能释放怒气技能)
     */
    FENG_YING(true, UnitState.IMMUNE_FENG_YING),
    /**
     * 禁止行动/晕(32)
     */
    DISABLE(true, UnitState.IMMUNE_DISABLE, UnitState.BA_TI),

    /**
     * 混乱/反选主动技能目标(128)
     */
    CHAOS(true, UnitState.IMMUNE_CHAOS, UnitState.BA_TI),

    /**
     * 物理免疫(512)
     */
    PHYSICS_IMMUNE(false, UnitState.IMMUNE_PHYSICS_IMMUNE),

    /**
     * 免疫:法术免疫(1024)
     */
    IMMUNE_MAGIC_IMMUNE(false),

    /**
     * 法术免疫(2048)
     */
    MAGIC_IMMUNE(false, UnitState.IMMUNE_MAGIC_IMMUNE),

    /**
     * 无敌
     */
    WU_DI(false),

    /**
     * 持有此状态的单位，可以被添加无敌，但身上的无敌无法生效
     */
    WU_DI_INVALID(true),


    /**
     * 沉默/禁技(8192)
     */
    SILENT(true, UnitState.IMMUNE_SILENT),


    /**
     * 嘲讽(32768)
     */
    SNEER(true, UnitState.IMMUNE_SNEER, UnitState.BA_TI),


    /**
     * 麻痹(131072)
     */
    PALSY(true, UnitState.IMMUNE_PALSY, UnitState.BA_TI),


    /**
     * 迷乱(524288)
     */
    CONFUSION(true, UnitState.IMMUNE_CONFUSION, UnitState.BA_TI),


    /**
     * 放逐(2097152)
     */
    EXILE(true, UnitState.IMMUNE_EXILE, UnitState.BA_TI),

    /**
     * 冻结
     */
    FROZEN(true, UnitState.IMMUNE_FROZEN, UnitState.BA_TI),

    /**
     * 缠绕(8388608)
     */
    BIND(true, UnitState.IMMUNE_BIND, UnitState.BA_TI),

    /**
     * 追击状态(16777216)
     */
    ZHUI_JI(false),

    /**
     * 跟随
     */
    FOLLOW(false),
    ;

    // 免疫值
    public final UnitState[] immune;

    // 有害状态，用于状态驱散
    public final boolean harm;

    UnitState(boolean harm, UnitState... immunes) {
        this.immune = immunes;
        this.harm = harm;
    }

    /**
     * 控制状态类型鉴定器
     */
    public static Set<UnitState> CONTROL_STATE;

    public boolean controlState() {
        return CONTROL_STATE.contains(this);
    }

    static {
        final Set<UnitState> controlState = new HashSet<>();
        for (UnitState state : UnitState.values()) {
            if (!state.harm) {
                continue;
            }
            for (UnitState immune : state.immune) {
                if (immune == BA_TI) {
                    controlState.add(state);
                }
            }
        }
        CONTROL_STATE = controlState;
    }
}
