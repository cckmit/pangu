package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.MiZongZhiDieRemakeParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 每次受到伤害时,指定技能降低冷却时间0.5秒
 */
@Component
public class MiZongZhiDieRemake implements DamagePassive {
    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (damage >= 0) {
            return;
        }
        final MiZongZhiDieRemakeParam param = passiveState.getParam(MiZongZhiDieRemakeParam.class);
        final List<SkillState> skillsByTag = owner.getActiveSkillsByTag(param.getSkillTag());
        for (SkillState state : skillsByTag) {
            state.setCd(state.getCd() + param.getCdChange());
        }
    }

    @Override
    public PassiveType getType() {
        return PassiveType.MI_ZONG_ZHI_DIE_REMAKE;
    }
}
