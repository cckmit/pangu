package com.pangu.logic.module.battle.service.buff.effect;


import com.pangu.logic.module.battle.model.report.BuffReport;
import com.pangu.logic.module.battle.service.buff.Buff;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.buff.param.JinZhanZheParam;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.skill.effect.BuffUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JinZhanZhe implements Buff {
    @Autowired
    private BuffUpdate buffUpdate;

    @Override
    public BuffType getType() {
        return BuffType.JIN_ZHAN_ZHE;
    }

    @Override
    public boolean add(BuffState state, Unit unit, int time) {
        unit.addBuff(state);
        return true;
    }

    @Override
    public void update(BuffState state, Unit unit, int time, Object addition) {
        final JinZhanZheParam param = state.getParam(JinZhanZheParam.class);
        //筛选指定区域内的条件单位
        final List<Unit> conditionUnits = TargetSelector.select(unit, param.getConditionTargetId(), time);
        //筛选指定区域内的目标单位
        final List<Unit> buffUnits = TargetSelector.select(unit, param.getBuffTargetId(), time);
        final BuffReport buffReport = state.getBuffReport();
        if(conditionUnits.size()>0){
            for (Unit buffUnit : buffUnits) {
                buffUpdate.doBuffUpdate(param.getBuffWhenHasUnit(), unit, buffUnit, buffReport, time);
            }
        } else{
            for (Unit buffUnit : buffUnits) {
                buffUpdate.doBuffUpdate(param.getBuffWhenNoUnit(), unit, buffUnit, buffReport, time);
            }
        }

    }
}
