package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.effect.MiDieHuaHaiBuff;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveFactory;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.MiDieHuaHaiParam;
import org.springframework.stereotype.Component;

/**
 * 在敌方人最多的敌方，种下一颗种子并使其迅速成长，情花爆炸开来,使敌方全体陷入迷醉,持续3秒，花香迷醉结束时会造成迷醉时受到伤害的25%的伤害
 * 2级:持续时间提升至4秒
 * 3级:迷醉结束时伤害提升至迷醉时伤害的40%
 * 4级:持续时间提升至5秒
 */
@Component
public class MiDieHuaHaiRemake implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.MI_DIE_HUA_HAI_REMAKE;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final MiDieHuaHaiParam param = state.getParam(MiDieHuaHaiParam.class);

        MiDieHuaHaiBuff.MiDieHuaHaiBuffAddition buffAddition = MiDieHuaHaiBuff.MiDieHuaHaiBuffAddition
                .of(param.getRate());
        BuffState buffState = BuffFactory
                .addBuff(param.getBuffId(), owner, target, time, skillReport, buffAddition);
        if (buffState != null) {
            PassiveState passiveState = PassiveFactory.initState(param.getPassiveId(), time);
            passiveState.setAddition(buffState);
            target.addPassive(passiveState, owner);
        }
    }
}
