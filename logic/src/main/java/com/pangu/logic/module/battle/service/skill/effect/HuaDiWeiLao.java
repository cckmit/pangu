package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.StateAdd;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveFactory;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.HuaDiWeiLaoParam;
import org.springframework.stereotype.Component;

/**
 * 维京战魂BOSS技能：画地为牢
 * 维京战魂制造一个结界，结界圈外英雄的造成的伤害-80%，持续15秒，15秒后自身进入眩晕5秒
 *
 * @author Kubby
 */
@Component
public class HuaDiWeiLao implements SkillEffect {

    @Override
    public EffectType getType() {
        return EffectType.HUA_DI_WEI_LAO;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time,
                        SkillState skillState, Context context) {
        HuaDiWeiLaoParam param = state.getParam(HuaDiWeiLaoParam.class);
        /* 地牢开始 */
        if (context.getLoopTimes() <= 1) {
            owner.addState(UnitState.NO_MOVE);
            PassiveState passiveState = PassiveFactory.initState(param.getPassiveId(), time);
            owner.addPassive(passiveState, owner);
            state.setAddition(passiveState);
        }
        /* 地牢结束 */
        else {
            owner.removeState(UnitState.NO_MOVE);
            PassiveState passiveState = state.getAddition(PassiveState.class);
            owner.removePassive(passiveState);
            state.setAddition(null);

            /* 进入眩晕状态，该技能为boss技能，boss自身带霸体，因此要跳过免疫鉴定直接添加状态 */
            final int expTime = time + param.getDisableDuration();
            owner.addState(UnitState.DISABLE, expTime);
            skillReport.add(time, owner.getId(), new StateAdd(UnitState.DISABLE, expTime));
        }
    }
}
