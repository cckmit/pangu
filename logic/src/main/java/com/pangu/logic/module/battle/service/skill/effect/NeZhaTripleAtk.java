package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.UnitRate;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.NeZhaTripleAtkParam;
import com.pangu.logic.module.battle.service.skill.param.StateAddParam;
import com.pangu.logic.module.battle.service.skill.utils.SkillUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 连续三次攻击，统共上一层火，第三次击飞且必定暴击
 */
@Component
public class NeZhaTripleAtk implements SkillEffect {
    @Autowired
    private HpPhysicsDamage hpPhysicsDamage;
    @Autowired
    private BuffUpdate buffUpdate;

    @Override
    public EffectType getType() {
        return EffectType.NEZHA_TRIPLE_ATK;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final NeZhaTripleAtkParam param = state.getParam(NeZhaTripleAtkParam.class);
        //更新火焰印记层数
        buffUpdate.doBuffUpdate(param.getBuffUpdateParam(), owner, target, skillReport, time);

        //造成伤害
        if (context.getLoopTimes() == skillState.getExecuteTimes()) {
            //最后一轮循环时添加异常状态和暴击奖励
            final StateAddParam stateAddParam = param.getStateAddParam();
            if (stateAddParam != null) {
                SkillUtils.addState(owner, target, stateAddParam.getState(), time, stateAddParam.getTime() + time, skillReport, context);
            }

            //追加暴击奖励
            if (param.isCritBonus()) {
                owner.increaseRate(UnitRate.CRIT, 10);
                state.setParamOverride(param.getDamageParam());
                hpPhysicsDamage.execute(state, owner, target, skillReport, time, skillState, context);
                state.setParamOverride(null);
                owner.increaseRate(UnitRate.CRIT, -10);
                return;
            }
        }
        state.setParamOverride(param.getDamageParam());
        hpPhysicsDamage.execute(state, owner, target, skillReport, time, skillState, context);
        state.setParamOverride(null);
    }
}
