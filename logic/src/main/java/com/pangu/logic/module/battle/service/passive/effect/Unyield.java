package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.skill.param.UnyieldParam;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Immune;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import org.springframework.stereotype.Component;

/**
 * {@link PassiveType#UNYIELD}类型的被动效果实现
 * 受到致死伤害时，将血量只保留1点血
 * 确保此被动优先级最低
 */
@Component
public class Unyield implements DamagePassive {

    @Override
    public PassiveType getType() {
        return PassiveType.UNYIELD;
    }

    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time, Context context, SkillState skillState, SkillReport skillReport) {
        //当具备[无敌失效]状态时，该被动不生效
        if (owner.hasState(UnitState.WU_DI_INVALID, time)) {
            return;
        }

        long totalDamage = context.getHpChange(owner);
        if (totalDamage >= 0) {
            return;
        }
        Integer expireTime = passiveState.getAddition(Integer.class);
        if (expireTime != null) {
            // 生效时间
            if (expireTime < time) {
                passiveState.addCD(time);
                passiveState.setAddition(null);
                if (passiveState.shouldRemove(time)) {
                    return;
                }
            } else {
                context.addPassiveValue(owner, AlterType.HP, -totalDamage);
                PassiveValue passiveValue = PassiveValue.single(passiveState.getId(), owner.getId(), new Immune());
                skillReport.add(time, owner.getId(), passiveValue);
                return;
            }
        }

        long value = owner.getValue(UnitValue.HP);

        long afterHp = totalDamage + value;
        if (afterHp > 0) {
            return;
        }

        // 更新记录状态
        UnyieldParam param = passiveState.getParam(UnyieldParam.class);
        int configTime = param.getTime();
        if (configTime > 0) {
            int expire = time + configTime;
            passiveState.setAddition(expire);
        } else {
            passiveState.addCD(time);
        }

        context.addPassiveValue(owner, AlterType.HP, -value - totalDamage + 1);
        PassiveValue passiveValue = PassiveValue.single(passiveState.getId(), owner.getId(), new Immune());
        skillReport.add(time, owner.getId(), passiveValue);

        String buffId = param.getBuffId();
        BuffFactory.addBuff(buffId, owner, owner, time, skillReport, null);
    }
}
