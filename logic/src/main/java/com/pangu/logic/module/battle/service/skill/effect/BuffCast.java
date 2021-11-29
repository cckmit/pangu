package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.common.resource.Formula;
import com.pangu.framework.resource.anno.Static;
import org.springframework.stereotype.Component;

/**
 * {@link EffectType#BUFF_CAST}效果的实现类
 */
@Component
public class BuffCast implements SkillEffect {

    //  技能是否施放成功
    @Static("FIGHT:BUFF:IS_SUCCESS")
    private Formula isSuccessFormula;

    @Override
    public EffectType getType() {
        return EffectType.BUFF_CAST;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        // 获取要施放的BUFF信息
        String buffId = state.getParam(String.class);
        BuffFactory.addBuff(buffId, owner, target, time, skillReport, null);
    }

}
