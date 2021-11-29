package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.model.report.values.PositionChange;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.FanJiZhiLianParam;
import com.pangu.logic.module.battle.service.skill.effect.HpPhysicsDamage;
import com.pangu.logic.module.battle.service.skill.effect.Repel;
import com.pangu.framework.utils.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * 反击之镰
 * 在被攻击时有20%的概率扔出飞镰,对命中的目标造成190%的物理伤害,并将他们击退至一边被命中的目标会受到减速效果,移动速度降低40%,持续3秒
 * 2级:伤害提升至230%
 * 3级:伤害提升至270%
 * 4级:概率提升至35%
 */
@Component
public class FanJiZhiLian implements DamagePassive {
    @Autowired
    private HpPhysicsDamage hpPhysicsDamage;
    @Autowired
    private Repel repel;

    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (damage >= 0) {
            return;
        }
        if (!attacker.canSelect(time)) {
            return;
        }
        if (owner == attacker) {
            return;
        }

        final FanJiZhiLianParam param = passiveState.getParam(FanJiZhiLianParam.class);
        //触发鉴定
        final boolean triggered = RandomUtils.isHit(param.getTriggerRate());
        if (!triggered) return;

        //造成伤害
        final EffectState dmgState = new EffectState(null, 0);
        dmgState.setParamOverride(param.getDmg());
        final HpPhysicsDamage.PhysicsDamageCalcResult result = hpPhysicsDamage.calcDamage(owner, attacker, skillState, dmgState, time);
        context.addPassiveValue(attacker, AlterType.HP, result.getDamage());
        final PassiveValue passiveValue = PassiveValue.of(passiveState.getId(), owner.getId());
        passiveValue.add(new Hp(result.getDamage(), result.isCrit(), result.isBlock()));
        skillReport.add(time, attacker.getId(), passiveValue);
        dmgState.setParamOverride(null);

        //击退
        final int repelDistance = param.getRepelDist();
        final PositionChange positionChange = new PositionChange();
        final boolean repelSuccess = repel.doRepel(repelDistance, owner, attacker, time, positionChange);
        if (repelSuccess) {
            passiveValue.add(positionChange);
        }

        //添加buff
        BuffFactory.addBuff(param.getBuffId(), owner, attacker, time, skillReport, null);
        //进入cd
        passiveState.addCD(time);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.FAN_JI_ZHI_LIAN;
    }
}
