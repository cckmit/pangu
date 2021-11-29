package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.HuaDiWeiLaoPassiveParam;
import org.springframework.stereotype.Component;

/**
 * 维京战魂BOSS技能：画地为牢
 * 维京战魂制造一个结界，结界圈外英雄的造成的伤害-80%，持续15秒，15秒后自身进入眩晕5秒
 * @author Kubby
 */
@Component
public class HuaDiWeiLaoPassive implements DamagePassive {

    @Override
    public PassiveType getType() {
        return PassiveType.HUA_DI_WEI_LAO;
    }

    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time,
                       Context context, SkillState skillState, SkillReport skillReport) {
        HuaDiWeiLaoPassiveParam param = passiveState.getParam(HuaDiWeiLaoPassiveParam.class);

        if (owner.getPoint().distance(attacker.getPoint()) <= param.getRange()) {
            return;
        }

        final long hpChange = context.getHpChange(owner);
        long increaseHp = (long) (-hpChange * param.getDecRate());
        context.addPassiveValue(owner, AlterType.HP, increaseHp);
        skillReport.add(time, owner.getId(), Hp.of(increaseHp));
    }

}
