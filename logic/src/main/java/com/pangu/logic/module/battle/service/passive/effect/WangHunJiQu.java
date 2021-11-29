package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.buff.utils.CalTypeHelper;
import com.pangu.logic.module.battle.service.buff.utils.CalValues;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.UnitDiePassive;
import com.pangu.logic.module.battle.service.passive.param.WangHunJiQuParam;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * 亡魂汲取(3级)
 * 每当周围有一个生物死亡,暗影魔主都会吸取他的灵魂来增加的攻击力。每死亡1个敌人，提升自己4%的攻击力，可叠加。
 * 2级：每死亡1个敌人，提升自己5%的攻击力
 * 3级：每死亡1个敌人，提升自己6%的攻击力
 * 4级：每死亡1个敌人，提升自己10%的攻击力
 */
@Component
public class WangHunJiQu implements UnitDiePassive {

    @Override
    public PassiveType getType() {
        return PassiveType.WANG_HUN_JI_QU;
    }

    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> dieUnits) {
        WangHunJiQuParam param = passiveState.getParam(WangHunJiQuParam.class);
        for (Unit target : dieUnits) {
            CalValues calValues = CalTypeHelper.calValues(param.getCalType(), param.getAlters(), owner, target, param.getFactor());
            Map<AlterType, Number> values = calValues.getValues();
            for (Map.Entry<AlterType, Number> entry : values.entrySet()) {
                AlterType alterType = entry.getKey();
                Number number = entry.getValue();
                context.addPassiveValue(owner, alterType, number);
                damageReport.add(time, owner.getId(), new UnitValues(alterType, number));
            }
        }
    }
}
