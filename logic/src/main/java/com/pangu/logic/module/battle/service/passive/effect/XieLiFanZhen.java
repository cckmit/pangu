package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.XieLiFanZhenParam;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import com.pangu.logic.module.battle.service.skill.effect.HpHigherDamage;
import com.pangu.logic.module.battle.service.skill.effect.HpPhysicsDamage;
import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import com.pangu.framework.utils.math.RandomUtils;
import com.pangu.framework.utils.reflect.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 械力反震
 * 受到攻击时有20%概率触发盾刺，对目标造成85%物理伤害
 * 2级：触发的概率提升至35%
 * 3级：触发时会击晕目标0.5秒。
 * 4级：对目标造成的伤害提升至120%
 */
@Component
public class XieLiFanZhen implements DamagePassive {
    @Autowired
    private HpHigherDamage higherDamage;
    @Autowired
    private HpPhysicsDamage physicsDamage;

    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (!attacker.canSelect(time)) {
            return;
        }

        if (owner == attacker) {
            return;
        }

        final XieLiFanZhenParam param = passiveState.getParam(XieLiFanZhenParam.class);
        //  新增传承强化特殊处理
        final String triggerBuff = param.getTriggerBuff();
        if (!StringUtils.isEmpty(triggerBuff) && !owner.getBuffBySettingId(triggerBuff).isEmpty()) {
            //  传承强化仅造成伤害
            context.passiveAtkDmg(owner, attacker, time, skillReport, higherDamage, new DamageParam(param.getFactor()), passiveState.getId(), null);
            return;
        }

        //触发鉴定
        final boolean triggered = RandomUtils.isHit(param.getTriggerRate());
        if (!triggered) {
            return;
        }

        //造成伤害
        final EffectState dmgState = new EffectState(null, 0);
        dmgState.setParamOverride(new DamageParam(param.getFactor()));
        final HpPhysicsDamage.PhysicsDamageCalcResult result = physicsDamage.calcDamage(owner, attacker, skillState, dmgState, time);
        final PassiveValue passiveValue = PassiveValue.of(passiveState.getId(), owner.getId());
        passiveValue.add(new Hp(result.getDamage(), result.isCrit(), result.isBlock()));
        context.addPassiveValue(attacker, AlterType.HP, result.getDamage());
        dmgState.setParamOverride(null);
        //进入cd
        passiveState.addCD(time);
        //添加异常
        if (param.getState() == null) {
            return;
        }
        PassiveUtils.addState(owner, attacker, param.getState(), param.getDuration() + time, time, context, passiveValue, skillReport);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.XIE_LI_FAN_ZHEN;
    }
}
