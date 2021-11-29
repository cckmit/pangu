package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.FilterType;
import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.action.EffectAction;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillSelectPassive;
import com.pangu.logic.module.battle.service.passive.param.FengBaoLianJiParam;
import com.pangu.logic.module.battle.service.select.filter.utils.AreaFilter;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class FengBaoLianJi implements SkillSelectPassive, AttackPassive {
    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (skillState.getType() != SkillType.SPACE) return;
        final int loops = context.getLoopTimes();
        final FengBaoLianJiParam param = passiveState.getParam(FengBaoLianJiParam.class);
        final Map<Integer, String> trigger = param.getTrigger();
        if (trigger == null || trigger.isEmpty()) return;
        for (Map.Entry<Integer, String> entry : trigger.entrySet()) {
            if (entry.getKey() == loops) {
                //调用效果
                if (entry.getValue() == null) continue;
                final EffectState effectState = SkillFactory.getEffectState(entry.getValue());
                final EffectAction effectAction = new EffectAction(time, owner, skillState, skillReport, effectState, Stream.of(target).collect(Collectors.toList()));
                owner.getBattle().addWorldAction(effectAction);
            }
        }
    }

    @Override
    public PassiveType getType() {
        return PassiveType.FENG_BAO_LIAN_JI;
    }

    @Override
    public SkillState skillSelect(PassiveState passiveState, SkillState skillState, Unit owner, int time) {
        if (skillState.getType() != SkillType.SPACE) return null;
        final FengBaoLianJiParam param = passiveState.getParam(FengBaoLianJiParam.class);
        final List<Unit> enemies = FilterType.ENEMY.filter(owner, time);
        if (AreaFilter.filterUnitInRelativeArea(enemies, param.getArea(), owner).size() > 0) return null;
        return SkillFactory.initState(param.getSkillId());
    }
}
