package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.AttackDeepenByTargetHpParam;
import org.springframework.stereotype.Component;

/**
 * 对方血量越低伤害越高
 */
@Component
public class AttackDeepenByTargetHp implements AttackPassive {
    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final AttackDeepenByTargetHpParam param = passiveState.getParam(AttackDeepenByTargetHpParam.class);
        final double loseHpRate = 1 - target.getHpPct();
        final double increaseRate = loseHpRate / param.getPreHpRate() * param.getIncreaseRate();
        final long increaseDamage = (long) (damage * increaseRate);
        if (increaseDamage <= 0) {
            return;
        }
        context.addPassiveValue(target, AlterType.HP, increaseDamage);
        skillReport.add(time, target.getId(), Hp.of(increaseDamage));
        passiveState.addCD(time);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.ATTACK_DEEPEN_BY_TARGET_HP;
    }
}
