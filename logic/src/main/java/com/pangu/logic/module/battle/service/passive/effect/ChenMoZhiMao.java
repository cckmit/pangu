package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.ChenMoZhiMaoParam;
import com.pangu.logic.module.battle.service.skill.effect.StateAddEffect;
import com.pangu.logic.module.battle.service.skill.param.StateAddParam;
import com.pangu.framework.utils.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 沉默之矛:
 * 每次攻击有15%的概率造成50%的额外伤害,并使受击目标沉默2秒
 * 2级:触发概率提升至20%
 * 3级:触发概率提升至25%
 * 4级:沉默时间提升至3.5秒
 */
@Component
public class ChenMoZhiMao implements AttackPassive {
    //此被动为对应主动效果的一部分，无需传Passive战报
    @Autowired
    private StateAddEffect stateAddEffect;

    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        //只有普攻才能触发
        if (skillState.getType() != SkillType.NORMAL) return;
        //触发鉴定
        final ChenMoZhiMaoParam param = passiveState.getParam(ChenMoZhiMaoParam.class);
        final boolean triggered = RandomUtils.isHit(param.getTriggerRate());
        if (!triggered) return;
        //增加伤害
        final long bonusDamage = (long) (damage * param.getDamageUpRate());
        context.addPassiveValue(target, AlterType.HP, bonusDamage);
        //添加沉默
        final UnitState stateType = param.getStateType();
        final int duration = param.getDuration();
        final StateAddParam stateAddParam = new StateAddParam();
        stateAddParam.setState(stateType);
        stateAddParam.setTime(duration);
        final EffectState effectState = new EffectState(null, 0);
        effectState.setParamOverride(stateAddParam);
        stateAddEffect.execute(effectState,owner,target,skillReport,time,skillState,context);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.CHEN_MO_ZHI_MAO;
    }
}
