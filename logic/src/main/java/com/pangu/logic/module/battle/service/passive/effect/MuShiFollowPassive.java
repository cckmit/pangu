package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.values.Follow;
import com.pangu.logic.module.battle.resource.BuffSetting;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.UnitDiePassive;
import com.pangu.logic.module.battle.service.passive.param.MuShiFollowPassiveParam;
import com.pangu.logic.module.battle.service.skill.effect.XingFenJiInitFollow;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 兴奋剂(3级)
 * 在战斗中，角色会始终跟随攻击力最高的存活队友移动，并使自己受到的伤害下降25%。
 */
@Component
public class MuShiFollowPassive implements UnitDiePassive {
    @Autowired
    private XingFenJiInitFollow follow;

    @Override
    public PassiveType getType() {
        return PassiveType.MU_SHI_FOLLOW;
    }

    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> dieUnits) {
        Unit traceUnit = owner.getTraceUnit();
        if (traceUnit == null || !traceUnit.isDead()) {
            return;
        }
        Unit followTarget = follow.searchFollow(owner, time);

        MuShiFollowPassiveParam param = passiveState.getParam(MuShiFollowPassiveParam.class);

        /* 专属装备BUFF */
        if (!StringUtils.isBlank(param.getZsBuffId())) {
            String zsBuffId = param.getZsBuffId();
            BuffSetting setting = BuffFactory.getSetting(zsBuffId);
            BuffState buffState = traceUnit.getBuffStateByTag(setting.getTag());
            if (buffState != null) {
                BuffFactory.removeBuffState(buffState, traceUnit, time);
            }
            if (followTarget != null) {
                BuffFactory.addBuff(zsBuffId, followTarget, owner, time, damageReport, null);
            }
        }

        String buffId = param.getBuffId();
        if (followTarget == null) {
            owner.setTraceUnit(null);
            owner.removeState(UnitState.FOLLOW);
            if (buffId == null) {
                return;
            }
            BuffSetting setting = BuffFactory.getSetting(buffId);
            BuffState buffState = owner.getBuffStateByTag(setting.getTag());
            if (buffState == null) {
                return;
            }
            BuffFactory.removeBuffState(buffState, owner, time);
            return;
        }
        // 添加跟随状态
        owner.setTraceUnit(followTarget);
        owner.addState(UnitState.FOLLOW);

        damageReport.add(time, owner.getId(), Follow.of(owner.getId(), followTarget.getId()));

        if (buffId == null) {
            return;
        }
        BuffSetting setting = BuffFactory.getSetting(buffId);
        BuffState buffState = owner.getBuffStateByTag(setting.getTag());
        if (buffState != null) {
            return;
        }
        BuffFactory.addBuff(buffId, owner, owner, time, damageReport, null);
    }
}
