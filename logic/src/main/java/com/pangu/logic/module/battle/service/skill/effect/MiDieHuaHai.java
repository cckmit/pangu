package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.PositionChange;
import com.pangu.logic.module.battle.service.action.BeginAction;
import com.pangu.logic.module.battle.service.action.SkillEffectAction;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.effect.MiDieHuaHaiBuff;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveFactory;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.select.select.utils.TwoPointDistanceUtils;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.MiDieHuaHaiParam;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * 蝴蝶仙子·莉亚娜技能：迷蝶花海
 * 1级：在空中来回飞舞一次,撒下花粉,使敌方全体陷入睡眠,持续3秒睡眠结束时会造成睡眠时受到伤害的25%的伤害
 * 2级：持续时间提升至4秒
 * 3级：睡眠结束时伤害提升至睡眠时伤害的30%
 * 4级：持续时间提升至5秒
 *
 * @author Kubby
 */
@Component
@Deprecated
public class MiDieHuaHai implements SkillEffect {

    @Override
    public EffectType getType() {
        return EffectType.MI_DIE_HUA_HAI;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit owner2, SkillReport skillReport, int time,
                        SkillState skillState, Context context) {
        SkillEffectAction skillEffectAction = context.getRootSkillEffectAction();

        MiDieHuaHaiParam param = state.getParam(MiDieHuaHaiParam.class);

        MiDieHuaHaiAddition addition = state.getAddition(MiDieHuaHaiAddition.class, new MiDieHuaHaiAddition());

        
        if (addition.nextMovePoint != null) {
            addition.nextMoveTime = time;
            effectMovePoint(owner, skillReport, addition, false);
        }

        if (addition.unreleases == null) {
            addition.unreleases = new LinkedList<>(owner.getEnemy().getCurrent());
        }

        
        Unit addEnemy = null;

        Iterator<Unit> iterator = addition.unreleases.iterator();
        while (iterator.hasNext()) {
            Unit enemy = iterator.next();
            if (enemy.isDead()) {
                iterator.remove();
                continue;
            }
            if (addEnemy == null) {
                addEnemy = enemy;
            } else {
                if (owner.getPoint().distance(enemy.getPoint()) < owner.getPoint().distance(addEnemy.getPoint())) {
                    addEnemy = enemy;
                }
            }
        }

        
        if (addEnemy == null) {
            effectMovePoint(owner, skillReport, addition, true);
            addition.unreleases = null;
            for (String buffId : param.getExecBuffIds()) {
                BuffFactory.removeBuffState(buffId, owner, time);
            }
            owner.removeState(UnitState.UNVISUAL);
            skillEffectAction.done();
            if (owner.getAction() != null && owner.getAction() instanceof BeginAction) {
                owner.reset(time + 1);
            }
            return;
        }

        
        int currDis = owner.getPoint().distance(addEnemy.getPoint());
        if (currDis <= param.getAddBuffRange()) {
            effectMovePoint(owner, skillReport, addition, true);
            MiDieHuaHaiBuff.MiDieHuaHaiBuffAddition buffAddition = MiDieHuaHaiBuff.MiDieHuaHaiBuffAddition
                    .of(param.getRate());
            BuffState buffState = BuffFactory
                    .addBuff(param.getBuffId(), owner, addEnemy, time, skillReport, buffAddition);
            if (buffState != null) {
                PassiveState passiveState = PassiveFactory.initState(param.getPassiveId(), time);
                passiveState.setAddition(buffState);
                addEnemy.addPassive(passiveState, owner);
            }
            
            addition.unreleases.remove(addEnemy);
        }
        
        else {
            int speed = (int) (owner.getValue(UnitValue.SPEED) * param.getMoveRate());
            int moveDis = Math.max(1, Math.min(currDis, speed));
            addition.nextMovePoint = TwoPointDistanceUtils
                    .getNearStartPoint(owner.getPoint(), moveDis, addEnemy.getPoint());
            addition.keepMoveCount++;
        }
    }

    private void effectMovePoint(Unit owner, SkillReport skillReport, MiDieHuaHaiAddition addition, boolean force) {
        if (addition.nextMovePoint != null) {
            if (force || addition.keepMoveCount >= 4) {
                owner.move(addition.nextMovePoint);
                final Point ownerPoint = owner.getPoint();
                skillReport.add(addition.nextMoveTime, owner.getId(),
                        new PositionChange(ownerPoint.getX(), ownerPoint.getY()));
                addition.nextMovePoint = null;
                addition.nextMoveTime = 0;
                addition.keepMoveCount = 0;
            }
        }
    }

    private static class MiDieHuaHaiAddition {

        private List<Unit> unreleases;

        private Point nextMovePoint;

        private int nextMoveTime;

        private int keepMoveCount;
    }

}
