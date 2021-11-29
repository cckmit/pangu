package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.RongYaoZhiLiParam;
import org.springframework.stereotype.Component;

/**
 * 荣耀之力
 * 血量高于50%时，攻击会附加自身当前生命值4%的伤害（最多额外造成攻击力*800%伤害），冷却3秒
 * 2级：伤害提升至5%
 * 3级：伤害提升至6%
 * 4级：冷却时间降低为2秒
 */
@Component
public class RongYaoZhiLi implements AttackPassive {
    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (damage >= 0) {
            return;
        }
        RongYaoZhiLiParam param = passiveState.getParam(RongYaoZhiLiParam.class);
        long hpMax = owner.getValue(UnitValue.HP_MAX);
        long hp = owner.getValue(UnitValue.HP);
        int percent = (int) (hp * 100 / hpMax);
        if (percent < param.getHpPercentHit()) {
            return;
        }
        long damageEnhance = (long) (damage * param.getDamageEnhance());

        long maxAttack = Math.max(owner.getValue(UnitValue.ATTACK_M), owner.getValue(UnitValue.ATTACK_P));
        long maxDamage = (long) (maxAttack * param.getMaxAttackEnhance());
        if (-damageEnhance > maxDamage) {
            damageEnhance = -maxDamage;
        }
        context.addPassiveValue(target, AlterType.HP, damageEnhance);
        skillReport.add(time, target.getId(), PassiveValue.single(passiveState.getId(), owner.getId(), Hp.of(damageEnhance)));
    }

    @Override
    public PassiveType getType() {
        return PassiveType.RONG_YAO_ZHI_LI;
    }
}
