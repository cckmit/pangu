package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.model.report.BuffReport;
import com.pangu.logic.module.battle.service.buff.Buff;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.buff.param.UpdateBuffParam;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.param.BuffUpdateParam;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.skill.effect.BuffUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UpdateBuffs implements Buff {
    @Autowired
    private BuffUpdate buffUpdate;

    @Override
    public BuffType getType() {
        return BuffType.UPDATE_BUFF;
    }

    @Override
    public boolean add(BuffState state, Unit unit, int time) {
        unit.addBuff(state);
        //立即执行一次
//        update(state, unit, time);
        return true;
    }

    @Override
    public void update(BuffState state, Unit unit, int time, Object o) {
        final UpdateBuffParam param = state.getParam(UpdateBuffParam.class);
        String targetId = param.getTargetId();
        if (targetId == null) targetId = "SELF";
        final List<Unit> targets = TargetSelector.select(unit, targetId, time);
        final BuffUpdateParam buffUpdateParam = param.getUpdateParam();
        final BuffReport buffReport = state.getBuffReport();
        for (Unit target : targets) {
            buffUpdate.doBuffUpdate(buffUpdateParam, unit, target, buffReport, time);
        }
    }
}
