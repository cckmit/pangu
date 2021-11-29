package com.pangu.logic.module.battle.service.skill.ctx;

import com.pangu.logic.module.battle.model.HeroRaceType;
import com.pangu.logic.module.battle.model.UnitRate;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.service.core.Unit;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class WrapperUnit {

    private Unit unit;

    // 比率属性增加
    private Map<UnitRate, Double> rateAdd;

    // 属性值比率变更
    private Map<UnitValue, Double> valueRate;

    public WrapperUnit(Unit unit) {
        this.unit = unit;
    }

    public long getValue(String type) {
        UnitValue unitValue = UnitValue.valueOf(type);
        long value = unit.getValue(unitValue);
        if (valueRate == null) {
            return value;
        }
        Double change = valueRate.get(unitValue);
        if (change == null) {
            return value;
        }
        return (long) (value * change);
    }

    //获取对指定种族的伤害加深（战斗公式中需要）
    public double getDeepen(HeroRaceType type) {
        return unit.getDeepen(type);
    }

    //获取单元指定种族（战斗公式中需要）
    public HeroRaceType getHeroRaceType() {
        return unit.getHeroRaceType();
    }

    public boolean hasState(String state, int time) {
        return unit.hasState(UnitState.valueOf(state), time);
    }


    //获取对指定性别的伤害加深（战斗公式中需要）
    public double getDeepen(boolean female) {
        return unit.getDeepen(female);
    }
    public boolean isFemale(){
        return unit.isFemale();
    }

    public double getRate(String type) {
        UnitRate unitRate = UnitRate.valueOf(type);
        double pre = unit.getRate(unitRate);
        if (rateAdd != null) {
            Double add = rateAdd.get(unitRate);
            if (add != null) {
                pre += add;
            }
        }
        return pre;
    }
}
