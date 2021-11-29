package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 批量添加BUFF
 */
@Component
public class BuffsCast implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.BUFFS_CAST;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        // 获取要施放的BUFF信息
        final String[] buffIds = state.getParam(String[].class);
        Arrays.stream(buffIds).forEach(buffId->BuffFactory.addBuff(buffId, owner, target, time, skillReport, null));
    }
}
