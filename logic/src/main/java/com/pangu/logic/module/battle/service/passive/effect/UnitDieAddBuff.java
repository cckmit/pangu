package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.UnitDiePassive;
import com.pangu.logic.module.battle.service.passive.param.UnitDieAddBuffParam;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.skill.effect.HpRecover;
import com.pangu.logic.module.battle.service.skill.param.HpRecoverParam;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 角色死亡时添加buff
 */
@Component
public class UnitDieAddBuff implements UnitDiePassive {
    @Autowired
    private HpRecover hpRecover;

    @Override
    public PassiveType getType() {
        return PassiveType.UNIT_DIE_ADD_BUFF;
    }

    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> dieUnits) {
        final UnitDieAddBuffParam param = passiveState.getParam(UnitDieAddBuffParam.class);
        //计算buff叠加次数
        long buffAddCount;
        switch (param.getDeadType()) {
            case ENEMY:
                buffAddCount = dieUnits.stream().filter(dead -> dead.getEnemy() == owner.getFriend()).count();
                break;
            case FRIEND:
                buffAddCount = dieUnits.stream().filter(dead -> dead.getFriend() == owner.getFriend()).count();
                break;
            case OTHER:
                buffAddCount = dieUnits.size();
                break;
            case ENEMY_HERO:
                buffAddCount = dieUnits.stream().filter(dead -> dead.getEnemy() == owner.getFriend() && dead.getSummonUnit() == null).count();
                break;
            case ENEMY_AROUND:
                buffAddCount = dieUnits.stream().filter(dead -> dead.getEnemy() == owner.getFriend() && dead.getPoint().distance(owner.getPoint()) <= param.getR()).count();
                break;
            case SUMMONED:
                buffAddCount = dieUnits.stream().filter(dead -> dead.getSummonUnit() == owner).count();
                break;
            default:
                return;
        }
        //获取目标单位
        final List<Unit> buffAddTargets = TargetSelector.select(owner, param.getTargetId(), time);
        //对每个目标进行死亡单位数量次数的处理
        for (Unit target : buffAddTargets) {
            for (int i = 0; i < buffAddCount; i++) {
                //为目标添加buff
                final String[] buffs = param.getBuffs();
                if (!ArrayUtils.isEmpty(buffs)) {
                    for (String buffId : buffs) {
                        BuffFactory.addBuff(buffId, owner, target, time, damageReport, null);
                    }
                }
                //为目标恢复生命
                final HpRecoverParam rcvParam = param.getRcvParam();
                if (rcvParam != null) {
                    final HpRecover.RecoverResult res = hpRecover.calcRecoverRes(owner, target, rcvParam);
                    context.passiveRecover(owner, target, res.getRecover(), time, passiveState, damageReport);
                }
            }
        }
        //控制cd
        passiveState.addCD(time);
    }
}
