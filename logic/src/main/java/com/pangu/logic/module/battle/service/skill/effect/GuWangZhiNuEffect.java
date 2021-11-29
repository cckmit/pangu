package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.action.custom.GuWangZhiNuAction;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveFactory;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.GuWangZhiNuParam;
import org.springframework.stereotype.Component;

/**
 * 骨王之怒
 * 数秒蓄力，蓄力期间降低受到的伤害并免疫控制，蓄力结束时攻击面前的所有敌人造成140%攻击力的
 * 伤害，该攻击额外造成蓄力期间自己所受伤害的200%。
 * 2级:伤害提升至160%攻击力
 * 3级:自身回复造成伤害的40%血量。
 * 4级:伤害提升至180%攻击力
 */
@Component
public class GuWangZhiNuEffect implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.GU_WANG_ZHI_NU;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        owner.addState(UnitState.BA_TI, skillState.getSingAfterDelay() + time);
        GuWangZhiNuParam param = state.getParam(GuWangZhiNuParam.class);
        // 使用被动来累计收到的伤害
        String passiveId = param.getPassive();
        PassiveState passiveState = PassiveFactory.initState(passiveId, time);
        target.addPassive(passiveState, owner);

        owner.addTimedAction(new GuWangZhiNuAction(time + param.getTime(), owner, skillState, state, skillReport));
    }


}
