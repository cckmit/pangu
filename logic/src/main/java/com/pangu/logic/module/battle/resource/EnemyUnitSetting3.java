package com.pangu.logic.module.battle.resource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pangu.logic.module.battle.model.HeroRaceType;
import com.pangu.logic.module.battle.model.ModelInfo;
import com.pangu.logic.module.battle.model.UnitRate;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.framework.resource.Validate;
import com.pangu.framework.resource.anno.Id;
import com.pangu.framework.resource.anno.Resource;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 单个个体的敌军配置
 */
@Resource("battle")
@Getter
public class EnemyUnitSetting3 implements Validate {
    //  唯一标识
    @Id
    private String id;
    //  模型信息
    private ModelInfo model;
    //  属性数值
    private HashMap<UnitValue, Long> values;
    //  几率集合
    private HashMap<UnitRate, Double> rates;

    // 属性值提升比率
    private HashMap<UnitValue, Double> valuesAdvanceRate;

    //  初始化状态
    private int state;
    //  可用技能配置
    private String[] skills;
    //  被动效果配置
    private String[] passives;
    //  初始化效果配置
    private String[] inits;

    private int level;

    private int fight;

    // 逻辑代码部分

    /**
     * 获取该配置项对应的战斗单位
     */
    public Unit toUnit(String unitId, String name) {
        Unit result = Unit.valueOf(model.copyOrClone(name), values, rates, state, skills, passives);
        result.setId(unitId);
        return result;
    }

    /**
     * 获取该配置项对应的战斗单位
     */
    public Unit toUnit(String unitId, String name, String[] additionSkills) {
        Unit result = Unit.valueOf(model.copyOrClone(name), values, rates, state, ArrayUtils.addAll(additionSkills, skills), passives);
        result.setId(unitId);
        return result;
    }

    @JsonIgnore
    public long getFight() {
        if (this.model != null) {
            return this.model.getFight();
        }
        return 0;
    }

    public void setFight(long fight) {
        if (this.model == null) {
            this.model = new ModelInfo();
        }
        this.model.setFight(fight);
    }


    public EnemyUnitSetting toEnemyUnitSetting() {
        EnemyUnitSetting setting = new EnemyUnitSetting();
        setting.setFight(this.fight);
        setting.setId(this.id);
        setting.setInits(this.inits);
        setting.setLevel(this.level);
        setting.setModel(this.model);
        setting.setPassives(this.passives);
        setting.setRates(this.rates);
        setting.setSkills(this.skills);
        setting.setState(this.state);
        setting.setValuesAdvanceRate(this.valuesAdvanceRate);
        setting.setValues(this.values);
        return setting;
    }

    @Override
    public boolean isValid() {
        if (valuesAdvanceRate != null && valuesAdvanceRate.size() > 0) {
            HashMap<UnitValue, Long> update = new HashMap<>(this.values);
            for (Map.Entry<UnitValue, Double> entry : valuesAdvanceRate.entrySet()) {
                UnitValue type = entry.getKey();
                Double rate = entry.getValue();
                Long pre = values.get(type);
                if (pre == null || pre == 0) {
                    continue;
                }
                update.put(type, (long) (pre * rate));
            }
            this.values = update;
        }
        final Long hp = this.values.get(UnitValue.HP);
        if (hp != null) {
            this.values.put(UnitValue.HP_MAX, hp);
        }
        if (model == null) {
            return false;
        } else {
            if (CollectionUtils.isEmpty(model.getProfessions())) {
                return false;
            }

            if (level != 0) {
                model.setLevel(level);
            }

            if (fight != 0) {
                model.setFight(fight);
            }

            addRaceDeepen(model.getRaceType());
        }
        return true;
    }

    private void addRaceDeepen(HeroRaceType raceType) {
        if (raceType == null) {
            return;
        }
        if (rates == null) {
            rates = new HashMap<>(1, 1);
        }
        switch (raceType) {
            case HYZD:
                rates.put(UnitRate.YBDG_DEEPEN, 0.15);
                break;
            case SYMJ:
                rates.put(UnitRate.YHSY_DEEPEN, 0.15);
                break;
            case YBDG:
                rates.put(UnitRate.ZXZC_DEEPEN, 0.15);
                break;
            case YHSY:
                rates.put(UnitRate.SYMJ_DEEPEN, 0.15);
                break;
            case ZRHX:
                rates.put(UnitRate.HYZD_DEEPEN, 0.15);
                break;
            case ZXZC:
                rates.put(UnitRate.ZRHX_DEEPEN, 0.15);
                break;
            default:
        }
    }
}
