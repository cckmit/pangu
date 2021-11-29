package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.RongLuZhiXinZSParam;
import com.pangu.logic.module.battle.service.skill.effect.HpRecover;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RongLuZhiXinZS implements DamagePassive {
    @Autowired
    private HpRecover hpRecover;

    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final RongLuZhiXinZSParam param = passiveState.getParam(RongLuZhiXinZSParam.class);
        if (param.getTriggerHpPct() <= owner.getHpPct()) {
            return;
        }
        //即使回血
        final HpRecover.RecoverResult recoverResult = hpRecover.calcRecoverRes(owner, owner, param.getHpRecoverParam());
        context.passiveRecover(owner, owner, recoverResult.getRecover(), time, passiveState, skillReport);
        //添加增益
        for (String buff : param.getBuffs()) {
            BuffFactory.addBuff(buff, owner, owner, time, skillReport, null);
        }
        passiveState.addCD(time);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.RONG_LU_ZHI_XIN_ZS;
    }
}
