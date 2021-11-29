package com.pangu.logic.module.battle.service.passive;

/**
 * 被动执行阶段
 */
public enum Phase {

    // 初始化，用于添加属性
    INIT,
    // 技能选择后
    SKILL_SELECT,
    // 攻击他人之前
    ATTACK_BEFORE,
    // 被他人攻击之前
    BE_ATTACK_BEFORE,
    // 攻击他人造成伤害
    ATTACK,
    //攻击结束之后
    ATTACK_END,
    // 受到伤害
    DAMAGE,

    // 被治疗
    RECOVER,
    //治疗他人
    RECOVER_TARGET,

    //自身死亡
    OWNER_DIE,

    // 死亡
    DIE(true),

    // 血量降低
    HP_DOWN(true),

    // 大招释放
    SKILL_RELEASE(true),

    // 被添加异常时;
    BE_STATE_ADD,
    // 添加异常时;
    STATE_ADD;

    // 监听器类型，会特殊处理
    public final boolean listener;

    Phase() {
        listener = false;
    }

    Phase(boolean listener) {
        this.listener = listener;
    }
}
