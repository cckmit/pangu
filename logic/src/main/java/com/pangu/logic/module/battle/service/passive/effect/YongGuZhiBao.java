package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.FilterType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.YongGuZhiBaoParam;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 同时处于城堡中的友方英雄受到伤害时平摊伤害（附属英雄的承伤量不会超过自身6%的生命上限）
 */
@Component
public class YongGuZhiBao implements DamagePassive {
    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final long hpChange = context.getHpChange(owner);
        if (hpChange >= 0) {
            return;
        }
        final YongGuZhiBaoParam param = passiveState.getParam(YongGuZhiBaoParam.class);
        final List<Unit> friends = FilterType.FRIEND.filter(owner, time);
        final List<Unit> marked = new ArrayList<>();
        for (Unit target : friends) {
            if (target.getBuffStateByTag(param.getWithBuffTag()) != null) {
                marked.add(target);
            }
        }
        if (marked.isEmpty()) {
            return;
        }

        //计算实际承伤并分摊伤害
        final long singleAlloc = hpChange / marked.size();
        long dmgDecrease = 0;

        final double hpPct = param.getHpPct();
        for (Unit friend : marked) {
            final long allocable = -(long) (hpPct * friend.getValue(UnitValue.HP_MAX));
            final long actAlloc = Math.max(singleAlloc, allocable);
            dmgDecrease -= actAlloc;
            PassiveUtils.hpUpdate(context, skillReport, owner, friend, actAlloc, time, passiveState);
        }

        PassiveUtils.hpUpdate(context, skillReport, owner, dmgDecrease, time);

        passiveState.addCD(time);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.YONG_GU_ZHI_BAO;
    }
}
