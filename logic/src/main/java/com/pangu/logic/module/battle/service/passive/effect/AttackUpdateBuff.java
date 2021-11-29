package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.BuffUpdateParam;
import com.pangu.logic.module.battle.service.skill.effect.BuffUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 攻击时对目标更新buff
 */
@Component
public class AttackUpdateBuff implements AttackPassive {
    @Autowired
    private BuffUpdate buffUpdate;

    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        buffUpdate.doBuffUpdate(passiveState.getParam(BuffUpdateParam.class), owner, target, skillReport, time);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.ATTACK_UPDATE_BUFF;
    }
}
