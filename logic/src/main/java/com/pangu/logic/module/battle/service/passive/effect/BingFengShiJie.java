package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.StateRemove;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.BingFengShiJieParam;
import com.pangu.logic.module.battle.service.skill.effect.StateAddEffect;
import com.pangu.logic.module.battle.service.skill.param.StateAddParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;


/**
 * 冰封世界
 * 释放时对全场敌人造成140%攻击力的伤害并使战场天气变为“冰雾”持续14秒。当处于“冰雾”天气时,敌入每秒受到40%攻击力的伤害且生命值低于30%的敌人将被冻结，直至其生命恢复至30%以上或冰雾天气结束
 * 2级:当处于冰雾天气时,敌人的生命恢复效果下降50%
 * 3级:“冰雾”天气持续时间增加至16秒
 * 4级:“冰雾”天气持续时间增加至18秒
 */
@Component("PASSIVE:BingFengShiJie")
public class BingFengShiJie implements AttackPassive {
    //该被动为主动技能效果的一部分，逻辑上不需要传被动id
    @Autowired
    private StateAddEffect stateAddEffect;

    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        //释放冰封世界时才触发
        if (!skillState.getTag().equals("bing_feng_shi_jie")) {
            return;
        }

        final Addition addition = getAddition(passiveState);
        final BingFengShiJieParam param = passiveState.getParam(BingFengShiJieParam.class);
        final StateAddParam state = param.getState();

        //目标生命值高于触发百分比时，解除冰冻
        if (target.getHpPct() >= param.getTriggerHpPct()) {
            final UnitState frozen = state.getState();
            if (target.hasState(frozen, time) && addition.stateAdd) {
                target.removeState(frozen);
                skillReport.add(time, target.getId(), new StateRemove(Collections.singletonList(frozen)));
                addition.stateAdd = false;
            }
            return;
        }

        //否则添加冰冻
        final EffectState effectState = new EffectState(null, 0);
        effectState.setParamOverride(state);
        stateAddEffect.execute(effectState, owner, target, skillReport, time, skillState, context);
        addition.stateAdd = true;
    }

    private Addition getAddition(PassiveState passiveState) {
        Addition addition = passiveState.getAddition(Addition.class);
        if (addition == null) {
            addition = new Addition();
            passiveState.setAddition(addition);
        }
        return addition;
    }

    private static class Addition {
        private boolean stateAdd;
    }

    @Override
    public PassiveType getType() {
        return PassiveType.BING_FENG_SHI_JIE;
    }
}
