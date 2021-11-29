package com.pangu.logic.module.battle.model.report.values;

import com.pangu.framework.protocol.annotation.Transable;
import lombok.*;

/**
 * 物理或者法术伤害
 */
@Getter
@Transable
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class Hp implements IValues {
    @Getter(AccessLevel.PRIVATE)
    private ValuesType type = ValuesType.HP;

    /**
     * 伤害值
     */
    private long damage;

    /**
     * 是否暴击
     */
    private boolean crit;

    /**
     * 是否格挡
     */
    private boolean block;

    /**
     * 是否为恢复效果
     */
    private boolean recover;

    public static Hp of(long damage) {
        Hp r = new Hp();
        r.damage = damage;
        return r;
    }

    public Hp(long damage, boolean crit, boolean block) {
        this.damage = damage;
        this.crit = crit;
        this.block = block;
    }

    public static Hp fromRecover(long damage, boolean crit, boolean block) {
        Hp hp = new Hp();
        hp.damage = damage;
        hp.crit = crit;
        hp.block = block;
        hp.recover = true;
        return hp;
    }
    public static Hp fromRecover(long damage) {
        Hp hp = new Hp();
        hp.damage = damage;
        hp.recover = true;
        return hp;
    }
}
