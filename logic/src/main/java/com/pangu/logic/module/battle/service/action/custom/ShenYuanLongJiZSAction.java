package com.pangu.logic.module.battle.service.action.custom;

import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.service.Battle;
import com.pangu.logic.module.battle.service.action.Action;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.skill.effect.ShenYuanLongJiZS;
import com.pangu.logic.module.battle.service.skill.param.ShenYuanLongJiZSParam;

import java.util.Iterator;
import java.util.Map;

/**
 * 深渊龙姬·安吉丽娜专属装备
 * 1：龙形态下，震慑所有敌人，敌人每秒降低20能量
 * 10：敌人每秒降低30能量
 * 20：敌人每秒降低40能量
 * 30：敌人每秒降低50能量
 *
 * @author Kubby
 */
public class ShenYuanLongJiZSAction implements Action {

    private int time;

    private Unit owner;

    private EffectState effectState;

    private ITimedDamageReport damageReport;

    @Override
    public int getTime() {
        return time;
    }

    @Override
    public void execute() {
        ShenYuanLongJiZSParam param = effectState.getParam(ShenYuanLongJiZSParam.class);
        ShenYuanLongJiZS.ShenYuanLongJiZSAddition addition = effectState.getAddition(
                ShenYuanLongJiZS.ShenYuanLongJiZSAddition.class);

        if (owner.isDead() || owner.getPassiveStateByType(PassiveType.SHEN_YUAN_MO_LONG) == null) {
            if (!addition.getBuffs().isEmpty()) {
                fixAddition(addition);
                for (Map.Entry<Unit, BuffState> entry : addition.getBuffs().entrySet()) {
                    Unit unit = entry.getKey();
                    BuffState buffState = entry.getValue();
                    BuffFactory.removeBuffState(buffState, unit, time);
                }
                addition.clearBuff();
            }
        } else {
            fixAddition(addition);
            for (Unit target : owner.getEnemy().getCurrent()) {
                if (addition.hasBuff(target)) {
                    continue;
                }
                BuffState buffState = BuffFactory.addBuff(param.getBuffId(), owner, target, time, damageReport, null);
                addition.addBuff(target, buffState);
            }
        }

        gotoNextAction();
    }

    private void fixAddition(ShenYuanLongJiZS.ShenYuanLongJiZSAddition addition) {
        Map<Unit, BuffState> buffs = addition.getBuffs();
        Iterator<Map.Entry<Unit, BuffState>> iterator = buffs.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Unit, BuffState> entry = iterator.next();
            Unit unit = entry.getKey();
            BuffState buffState = entry.getValue();
            if (unit.getBuffStateByTag(buffState.getTag()) == null) {
                iterator.remove();
            }
        }
    }

    private void gotoNextAction() {
        ShenYuanLongJiZSParam param = effectState.getParam(ShenYuanLongJiZSParam.class);
        Battle battle = owner.getBattle();
        time += Math.max(500, param.getInterval());
        battle.addWorldAction(this);
    }

    public static ShenYuanLongJiZSAction of(int time, Unit owner, EffectState effectState, ITimedDamageReport damageReport) {
        ShenYuanLongJiZSAction action = new ShenYuanLongJiZSAction();
        action.time = time;
        action.owner = owner;
        action.effectState = effectState;
        action.damageReport = damageReport;
        return action;
    }
}
