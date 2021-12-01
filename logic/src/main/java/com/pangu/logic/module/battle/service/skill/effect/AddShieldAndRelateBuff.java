package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.action.custom.ShieldRelateBuffAction;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.AddShieldAndRelateBuffParam;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

/**
 * 添加护盾，并与BUFF关联（如果身上没了护盾，将移除相关BUFF）
 * @author Kubby
 */
@Component
public class AddShieldAndRelateBuff implements SkillEffect {

    @Override
    public EffectType getType() {
        return EffectType.ADD_SHIELD_AND_RELATE_BUFF;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time,
                        SkillState skillState, Context context) {

        ShieldRelateBuffAction action = state.getAddition(ShieldRelateBuffAction.class);
        if (action != null) {
            action.cancel(time);
            state.setAddition(null);
        }

        AddShieldAndRelateBuffParam param = state.getParam(AddShieldAndRelateBuffParam.class);


        BuffFactory.addBuff(param.getShieldBuffId(), owner, target, time, skillReport, null);

        if (param.getBuffs().isEmpty()) {
            return;
        }

        List<BuffState> buffStates = new LinkedList<>();
        for (String buffId : param.getBuffs()) {
            BuffState buffState = BuffFactory.addBuff(buffId, owner, target, time, skillReport, null);
            buffStates.add(buffState);
        }


        ShieldRelateBuffAction newAction = ShieldRelateBuffAction.of(time + 1000, target, buffStates);
        target.addTimedAction(newAction);
    }
}
