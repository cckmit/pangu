package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.*;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.PositionChange;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.BattleConstant;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.select.utils.TwoPointDistanceUtils;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import com.pangu.logic.module.battle.service.skill.param.YingXiongJiangLinParam;
import com.pangu.logic.module.battle.service.skill.utils.SkillUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 英雄降临：
 * 奥米茄将一名友军的当前位置作为他的着陆点，并为区域内的所有友军提供一个魔法护盾。
 * 在短暂的延迟后，奥米茄会落到该位置上，并对周围的敌人造成200%的物理伤害
 * 2级：护盾值提升到200%
 * 3级：范围伤害提升至300%
 * 4级：落地时会同时将周围的敌军击飞
 */
@Component
public class YingXiongJiangLin implements SkillEffect {

    @Autowired
    private HpPhysicsDamage physicsDamage;

    @Override
    public EffectType getType() {
        return EffectType.YING_XIONG_JIANG_LIN;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        YingXiongJiangLinParam param = state.getParam(YingXiongJiangLinParam.class);
        int radius = param.getRadius();
        Point point = target.getPoint();
        List<Unit> validEnemy = withinDistance(point, FilterType.ENEMY.filter(owner, time), radius, null);
        DamageParam damageParam = new DamageParam(param.getFactor());
        state.setParamOverride(damageParam);
        int disableTime = time + param.getDisableTime();
        //伤害与控制
        for (Unit unit : validEnemy) {
            physicsDamage.execute(state, owner, unit, skillReport, time, skillState, context);
            if (param.getDisableTime() <= 0) {
                continue;
            }
            SkillUtils.addState(owner, unit, UnitState.DISABLE, time, disableTime, skillReport, context);
        }
        state.setParamOverride(null);
        //位置
        final Point ownerPoint = owner.getPoint();
        final Point destPoint = TwoPointDistanceUtils.getNearStartPoint(point, BattleConstant.SCOPE, ownerPoint);
        owner.move(destPoint);
        skillReport.add(time, owner.getId(), PositionChange.of(ownerPoint.x, ownerPoint.y));

        //护盾
        List<Unit> validFriend = withinDistance(point, FilterType.FRIEND.filter(owner, time), radius, owner);
        if (validFriend.isEmpty()) {
            return;
        }
        long attack = Math.max(owner.getValue(UnitValue.ATTACK_M), owner.getValue(UnitValue.ATTACK_P));
        long shieldSet = (long) (attack * param.getShieldByAttack());
        for (Unit friendUnit : validFriend) {
            context.addValue(friendUnit, AlterType.SHIELD_SET, shieldSet);
            skillReport.add(time, friendUnit.getId(), new UnitValues(AlterType.SHIELD_SET, shieldSet));
        }
    }

    private List<Unit> withinDistance(Point from, List<Unit> current, int radius, Unit owner) {
        List<Unit> units = new ArrayList<>(current.size());
        for (Unit unit : current) {
            if (unit == owner) {
                continue;
            }
            int distance = from.distance(unit.getPoint());
            if (distance <= radius) {
                units.add(unit);
            }
        }
        return units;
    }
}
