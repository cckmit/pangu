package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.ShaZhiBianTaParam;
import com.pangu.logic.module.battle.service.skill.effect.MpChange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 沙之鞭挞
 * 挥舞法杖鞭挞前方,造成120%攻击力的无视敌我的范围伤害若友军受到此技能伤害会恢复65点能量并获得20%的急速效果持续5秒
 * 2级:伤害提升至130%攻击力
 * 3级:该技能造成伤害的40%转化为自身生命值
 * 4级:伤害提升至140%攻击力
 */
@Component
public class ShaZhiBianTa implements AttackPassive {
    @Autowired
    private Suck suck;
    @Autowired
    private MpChange mpChange;
    @Override
    public PassiveType getType() {
        return PassiveType.SHA_ZHI_BIAN_TA;
    }

    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (!skillState.getTag().equals("sha_zhi_bian_ta")) {
            return;
        }
        final ShaZhiBianTaParam param = passiveState.getParam(ShaZhiBianTaParam.class);
        //若命中友方添加增益
        if (target.getFriend() == owner.getFriend()) {
            //添加buff
            BuffFactory.addBuff(param.getBuffId(), owner, target, time, skillReport, null);
            //回能
            final EffectState effectState = new EffectState(null, 0);
            effectState.setParamOverride(param.getMp());
            mpChange.execute(effectState,owner,target,skillReport,time,skillState,context);
        }
        //回复生命
        passiveState.setParamOverride(param.getSuckRate());
        suck.attack(passiveState, owner, target, damage, time, context, skillState, skillReport);
        passiveState.setParamOverride(null);
    }
}
