package com.pangu.logic.module.battle.model.report.values;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.framework.protocol.annotation.Transable;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 被动触发的属性
 */
@Getter
@Transable
public class PassiveValue implements IValues {
    @Getter(AccessLevel.PRIVATE)
    private ValuesType type = ValuesType.PASSIVE;

    private PassiveValue() {
    }

    /**
     * 被动ID
     */
    private String passive;

    /**
     * 变更属性
     */
    private List<IValues> values;

    /**
     * 被动释放者
     */
    private String owner;

    public void add(IValues value) {
        if (this.values == null) {
            this.values = new ArrayList<>(4);
        }

        OUT:
        if (value instanceof UnitValues) {//血量变更统一使用Hp类型
            UnitValues unitValues = (UnitValues) value;
            if (unitValues.getAlterType() == AlterType.HP) {
                value = Hp.of(unitValues.getValue().longValue());
                break OUT;
            }
            if (unitValues.getAlterType() == AlterType.MP) {
                value =  new Mp(unitValues.getValue().longValue());
                break OUT;
            }
        }

        this.values.add(value);
    }

    public static PassiveValue of(String passiveId, String owner) {
        PassiveValue v = new PassiveValue();
        v.passive = passiveId;
        v.owner = owner;
        return v;
    }

    public static PassiveValue single(String passiveId, String owner, IValues value) {
        PassiveValue r = new PassiveValue();
        r.passive = passiveId;
        r.owner = owner;
        r.values = Collections.singletonList(value);
        return r;
    }
}
