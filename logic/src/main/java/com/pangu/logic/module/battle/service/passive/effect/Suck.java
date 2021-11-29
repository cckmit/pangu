package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import org.springframework.stereotype.Component;

/**
 * {@link PassiveType#SUCK}类型的被动效果实现
 */
@Component
public class Suck implements AttackPassive {

    @Override
    public PassiveType getType() {
        return PassiveType.SUCK;
    }

    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (damage >= 0) {
            return;    // 没有产生伤害
        }

        double rate = passiveState.getParam(Double.class);
        if (rate <= 0) {
            return;
        }

        // 计算吸血量
        int value = (int) (-context.getHpChange(target) * rate);
        context.addPassiveValue(owner, AlterType.HP, value);
        // 生成战报信息
        skillReport.add(time, owner.getId(), PassiveValue.single(passiveState.getId(), owner.getId(), Hp.fromRecover(value)));
    }
}
