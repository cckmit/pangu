package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import org.springframework.stereotype.Component;

/**
 * 骨王之怒
 * 数秒蓄力，蓄力期间降低受到的伤害并免疫控制，蓄力结束时攻击面前的所有敌人造成140%攻击力的
 * 伤害，该攻击额外造成蓄力期间自己所受伤害的200%。
 * 2级:伤害提升至160%攻击力
 * 3级:自身回复造成伤害的40%血量。
 * 4级:伤害提升至180%攻击力
 */
@Component
public class GuWangZhiNuPassive implements DamagePassive {
    @Override
    public PassiveType getType() {
        return PassiveType.GU_WANG_ZHI_NU;
    }

    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time, Context context, SkillState skillState, SkillReport skillReport) {
        Long preDamage = passiveState.getAddition(Long.class);
        if (preDamage == null) {
            preDamage = 0L;
        }
        passiveState.setAddition(preDamage + damage);
    }
}
