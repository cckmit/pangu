package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.YueRenCaiJueParam;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import com.pangu.logic.module.battle.service.skill.effect.BuffUpdate;
import com.pangu.logic.module.battle.service.skill.effect.StateAddEffect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 月刃裁决
 * 召唤月之刃攻击敌方审判印记最多的英雄,造成250%攻击力的伤害并给对方施加1层印记；当施放时目标有3层以上审判印记额外眩晕敌人1.5秒；5层审判印记时,清除敌人的审判印记,并使断罪审判的伤害提高300%
 * 2级:伤害提升至280%攻击力
 * 3级:伤害提升至310%攻击力
 * 4级:眩晕时间提升至2.5秒
 *
 * 此【被动】用于处理【对应主动技能的增伤和眩晕效果】
 * 其余效果由相关【主动】效果实现
 * {@link com.pangu.logic.module.battle.service.skill.effect.YueRenCaiJue}
 */
@Component("PASSIVE:YueRenCaiJue")
public class YueRenCaiJue implements AttackPassive {
    //该被动为其对应主动效果的一部分，添加状态无需传Passive战报
    @Autowired
    private StateAddEffect stateAddEffect;
    @Autowired
    private BuffUpdate buffUpdate;

    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (skillState.getType() != SkillType.SPACE) {
            return;
        }
        final YueRenCaiJueParam param = passiveState.getParam(YueRenCaiJueParam.class);
        final BuffState buffState = target.getBuffStateByTag("shen_pan_yin_ji");

        //根据目标身上的印记数量执行不同效果
        if (buffState != null) {
            final Integer count = buffState.getAddition(Integer.class);
            if (count >= param.getStateAddTriggerCount()) {
                final EffectState effectState = new EffectState(null, 0);
                effectState.setParamOverride(param.getState());
                stateAddEffect.execute(effectState, owner, target, skillReport, time, skillState, context);
            }
            if (count >= param.getMarkResetTriggerCount()) {
                buffState.setAddition(0);
            }
            if (damage >= 0) {
                return;
            }
            if (count >= param.getDmgUpTriggerCount()) {
                final long dmgUp = (long) (damage * param.getDmgUpRate());
                PassiveUtils.hpUpdate(context,skillReport,target,dmgUp,time);
            }
        }

        //该主动技能本身也会添加印记
        final EffectState effectState = new EffectState(null, 0);
        effectState.setParamOverride(param.getBuff());
        buffUpdate.execute(effectState,owner,target,skillReport,time,skillState,context);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.YUE_REN_CAI_JUE;
    }
}
