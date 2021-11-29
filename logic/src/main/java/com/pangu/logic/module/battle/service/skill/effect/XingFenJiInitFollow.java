package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.FilterType;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Follow;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveFactory;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.param.MuShiFollowPassiveParam;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 兴奋剂(3级)
 * 在战斗中，角色会始终跟随攻击力最高的存活队友移动
 */
@Component
public class XingFenJiInitFollow implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.XING_FEN_JI_FOLLOW;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        Unit followTarget = searchFollow(owner, time);
        if (followTarget == null) {
            owner.setTraceUnit(null);
            return;
        }
        // 添加跟随状态
        owner.setTraceUnit(followTarget);
        owner.addState(UnitState.FOLLOW);

        skillReport.add(time, owner.getId(), Follow.of(owner.getId(), followTarget.getId()));

        String param = state.getParam(String.class);
        PassiveState passiveState = PassiveFactory.initState(param, time);
        target.addPassive(passiveState, owner);

        MuShiFollowPassiveParam muShiFollowPassiveParam = passiveState.getParam(MuShiFollowPassiveParam.class);
        if (StringUtils.isEmpty(muShiFollowPassiveParam.getBuffId())) {
            return;
        }
        BuffFactory.addBuff(muShiFollowPassiveParam.getBuffId(), owner, owner, time, skillReport, null);
    }

    public Unit searchFollow(Unit owner, int time) {
        final List<Unit> allLive = FilterType.FRIEND.filter(owner, time);
        // 只有自己一个单元，不进行跟随
        if (allLive.size() == 1) {
            return null;
        }
        long maxAtk = 0;
        Unit followTarget = null;
        for (Unit unit : allLive) {
            if (unit == owner) {
                continue;
            }
            if (unit.isDead()) {
                continue;
            }
            if (unit.isSummon()) {
                continue;
            }
            long curAtk = Math.max(unit.getValue(UnitValue.ATTACK_P), unit.getValue(UnitValue.ATTACK_M));
            if (curAtk >= maxAtk) {
                followTarget = unit;
                maxAtk = curAtk;
            }
        }
        return followTarget;
    }
}
