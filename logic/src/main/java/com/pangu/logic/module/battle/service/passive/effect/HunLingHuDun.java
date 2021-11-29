package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.service.action.Action;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Fighter;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.UnitHpChangePassive;
import com.pangu.logic.module.battle.service.passive.param.HunLingHuDunParam;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * 魂灵护盾
 * 任意友军血量低于10%时，给他施加护罩，免疫一切伤害，持续2秒。每个友军一场战斗只能获得1次
 * 2级：时间提升至3秒
 * 3级：时间提升至4秒
 * 4级：时间提升至5秒
 */
@Component
public class HunLingHuDun implements UnitHpChangePassive {
    @Override
    public void hpChange(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> changeUnit) {
        HunLingHuDunParam param = passiveState.getParam(HunLingHuDunParam.class);
        //noinspection unchecked
        Set<String> validIds = passiveState.getAddition(Set.class, new HashSet<String>());
        Fighter friend = owner.getFriend();

        boolean triggered = false;
        for (Unit unit : changeUnit) {
            if (unit.getFriend() != friend) {
                continue;
            }
            String targetId = unit.getId();
            if (validIds.contains(targetId)) {
                continue;
            }
            long hpChange = context.getHpChange(unit);
            long curHp = unit.getValue(UnitValue.HP);
            long curPercent = (curHp + hpChange) * 100 / unit.getValue(UnitValue.HP_MAX);
            if (curPercent > param.getHpPercent()) {
                continue;
            }
            //目标已死亡不会触发该被动
            if (unit.isDead()) {
                continue;
            }
            triggered = true;
            int validTime = time + param.getTime();
            //不免疫当次伤害
            final Action addState = new Action() {
                @Override
                public int getTime() {
                    return time + 1;
                }

                @Override
                public void execute() {
                    if (unit.isDead()) {
                        return;
                    }
                    PassiveUtils.addState(owner, unit, UnitState.WU_DI, validTime, time + 1, passiveState, context, damageReport);
                    validIds.add(targetId);
                }
            };
            unit.addTimedAction(addState);
        }

        //未触发成功，不计算cd
        if (!triggered) {
            return;
        }
        passiveState.addCD(time);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.HUN_LING_HU_DUN;
    }
}
