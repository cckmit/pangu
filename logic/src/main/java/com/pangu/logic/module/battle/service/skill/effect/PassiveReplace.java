package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.resource.PassiveSetting;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.PassiveReplaceParam;
import org.springframework.stereotype.Component;

/**
 * 将指定前缀的被动替换为相同等级的另一个被动
 */
@Component
public class PassiveReplace implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.PASSIVE_REPLACE;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final PassiveReplaceParam param = state.getParam(PassiveReplaceParam.class);
        //  移除旧被动
        final String replacedPassiveId = PassiveSetting.toPassiveId(target, param.getReplacedPrefix());
        final boolean removed = target.removePassive(replacedPassiveId);
        if (!removed) {
            return;
        }

        //  添加新被动
        final int lv = PassiveSetting.getLv(replacedPassiveId);
        final String replacingPassiveId = PassiveSetting.toPassiveId(param.getReplacingPrefix(), lv);
        target.addPassive(replacingPassiveId, time, owner);
    }
}
