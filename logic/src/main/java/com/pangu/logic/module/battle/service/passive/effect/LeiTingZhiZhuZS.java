package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.LeiTingZhiZhuZSParam;
import com.pangu.logic.module.battle.service.skill.effect.BuffUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 1：生命值高于50%时，受到所有负面效果的时间-30%
 * 10：普攻暴击附带1层感电
 * 20：生命值高于50%时，受到所有负面效果的时间-50%
 * 30：普攻暴击附带2层感电
 */
@Component
public class LeiTingZhiZhuZS implements AttackPassive, DamagePassive {

    @Autowired
    private BuffUpdate buffUpdate;

    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (skillState.getType() != SkillType.NORMAL) {
            return;
        }
        if (!context.isCrit(target)) {
            return;
        }
        final EffectState effectState = new EffectState(null, 0);
        effectState.setParamOverride(passiveState.getParam(LeiTingZhiZhuZSParam.class).getBuffUpdateWhenCrit());
        buffUpdate.execute(effectState, owner, target, skillReport, time, skillState, context);
    }

    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final LeiTingZhiZhuZSParam param = passiveState.getParam(LeiTingZhiZhuZSParam.class);
        final BuffState buffState = owner.getBuffStateByTag("lei_ting_zhi_zhu_zs");
        if (owner.getHpPct() >= param.getHarmfulEffectCutTriggerHpPct()) {
            if (buffState != null) {
                BuffFactory.removeBuffState(buffState, owner, time);
            }
        } else {
            if (buffState != null){
                return;
            }
            BuffFactory.addBuff(param.getBuffAddWhenHarmfulEffectCutTrigger(), owner, owner, time, skillReport, null);
        }
    }

    @Override
    public PassiveType getType() {
        return PassiveType.LEI_TING_ZHI_ZHU_ZS;
    }
}
