package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveFactory;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import com.pangu.logic.module.battle.service.skill.param.WuJinShaLuParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 无尽杀戮的深渊:
 * 对目标造成140%攻击力无视防御与免疫的伤害,如果目标生命值低于40%则造成4倍伤害
 * 2级:成功击杀目标时,额外恢复250点能量如果被击杀的目标是召唤物则变为额外恢复700点能量
 * 3级:成功击杀目标时,额外获得一个350%攻击力的护盾
 * 4级:伤害提升至170%
 */
@Component
@Deprecated
public class WuJinShaLu implements SkillEffect {

    @Autowired
    private HpPhysicsDamage physicsDamage;

    @Override
    public EffectType getType() {
        return EffectType.WU_JIN_SHA_LU;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        WuJinShaLuParam param = state.getParam(WuJinShaLuParam.class);
        DamageParam damageParam = new DamageParam(param.getFactor(), param.getCritExp(), param.getOwnerAddRate(), param.getTargetValueChangeRate());
        state.setParamOverride(damageParam);
        physicsDamage.execute(state, owner, target, skillReport, time, skillState, context);
        state.setParamOverride(null);
        long hpRate = target.getValue(UnitValue.HP) * 100 / target.getValue(UnitValue.HP_MAX);
        if (hpRate <= param.getHpRateDamageDeepenPercent()) {
            long hp = context.getOriginHpChange(target) * param.getHpRateDamageDeepen();
            context.addValue(target, AlterType.HP, hp);
        }
        String passiveId = param.getPassiveId();
        if (StringUtils.isEmpty(passiveId)) {
            return;
        }
        // 击杀增加怒气，需要一个单独的被动
        PassiveState passiveState = PassiveFactory.initState(passiveId, time);
        owner.addPassive(passiveState, owner);
    }
}
