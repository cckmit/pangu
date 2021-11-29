package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.*;
import com.pangu.logic.module.battle.service.passive.param.YueZhiZhanJiParam;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import com.pangu.logic.module.battle.service.skill.effect.MpChange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 月之斩击
 * 使用月刃裁决后,下一次平砍会造成200%伤害,并回复额外能量100
 * 2级:伤害提升至250%
 * 3级:伤害提升至300%
 * 4级:能量回复提升200
 */
@Component
public class YueZhiZhanJi implements AttackPassive, SkillReleasePassive {
    @Autowired
    private MpChange mpChange;

    //普攻处于强化状态时增伤+额外回能
    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (skillState.getType() != SkillType.NORMAL) {
            return;
        }
        final Addition addition = getAddition(passiveState);
        if (addition.strengthenState != Addition.State.CONSUMING) {
            return;
        }
        if (damage >= 0) {
            return;
        }
        final YueZhiZhanJiParam param = passiveState.getParam(YueZhiZhanJiParam.class);
        final long dmgUp = (long) (damage * (param.getDmgRate() - 1));
        PassiveUtils.hpUpdate(context, skillReport, owner, target, dmgUp, time, passiveState);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.YUE_ZHI_ZHAN_JI;
    }

    //本被动的状态机
    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time, Context context, SkillReport skillReport) {
        final Addition addition = getAddition(passiveState);
        final YueZhiZhanJiParam param = passiveState.getParam(YueZhiZhanJiParam.class);

        switch (addition.strengthenState) {
            //释放大招后充能
            case CONSUMED: {
                if (skillState.getType() == SkillType.SPACE) {
                    addition.strengthenState = Addition.State.CONSUMABLE;
                }
                break;
            }
            //充能后的下一次普攻会被强化
            case CONSUMABLE: {
                if (skillState.getType() == SkillType.NORMAL) {
                    final EffectState effectState = new EffectState(null, 0);
                    effectState.setParamOverride(param.getMp());
                    mpChange.execute(effectState, owner, owner, skillReport, time, skillState, context);
                    addition.strengthenState = Addition.State.CONSUMING;
                }
                break;
            }
            //强化状态结束后的下一次技能如果为大招，则立即充能，否则强化状态切换为已消耗
            case CONSUMING: {
                if (skillState.getType() == SkillType.SPACE) {
                    addition.strengthenState = Addition.State.CONSUMABLE;
                } else {
                    addition.strengthenState = Addition.State.CONSUMED;
                }
            }
        }
    }

    private Addition getAddition(PassiveState passiveState) {
        Addition addition = passiveState.getAddition(Addition.class);
        if (addition == null) {
            addition = new Addition();
            passiveState.setAddition(addition);
        }
        return addition;
    }

    private static class Addition {
        private State strengthenState = State.CONSUMED;

        private enum State {
            CONSUMABLE, CONSUMING, CONSUMED
        }
    }
}
