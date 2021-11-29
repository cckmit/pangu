package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.*;
import com.pangu.logic.module.battle.service.passive.param.JiBingNvHuangZSParam;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import com.pangu.logic.module.battle.service.skill.param.StateAddParam;
import com.pangu.framework.utils.math.RandomUtils;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 1：每次普通攻击时都有15%的概率使目标冰冻1秒（攻击召唤物时概率为50%，且造成的伤害提高100%）
 * 10：普攻的冰冻概率提升至35%（攻击召唤物时概率为50%，且造成的伤害提高100%）
 * 20：释放冰封世界时，100%的概率冰冻可被攻击的召唤物
 * 30：每击杀一个召唤物，获得200点能量
 */
@Component
public class JiBingNvHuangZS implements UnitDiePassive, AttackPassive, AttackEndPassive {

    //增伤+冰冻
    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (skillState.getType() != SkillType.NORMAL) {
            return;
        }
        final JiBingNvHuangZSParam param = passiveState.getParam(JiBingNvHuangZSParam.class);

        //对召唤物特殊对待
        double dmgUp = 0;
        double triggerProb;
        if (target.isSummon()) {
            dmgUp = param.getDmgUpWhenAttackSummonUnit();
            triggerProb = param.getTriggerProbWhenAttackSummonUnit();
        } else {
            triggerProb = param.getTriggerProb();
        }
        //执行增伤
        PassiveUtils.hpUpdate(context, skillReport, target, (long) (damage * dmgUp), time);
        //执行异常
        if (!RandomUtils.isHit(triggerProb)) {
            return;
        }
        final StateAddParam state = param.getState();
        PassiveUtils.addState(owner, target, state.getState(), state.getTime() + time, time, passiveState, context, skillReport);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.JI_BING_NV_HUANG_ZS;
    }

    //击杀召唤物额外回能
    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> dieUnits) {
        if (attacker != owner) {
            return;
        }
        int count = 0;
        for (Unit dieUnit : dieUnits) {
            if (dieUnit.isSummon()) {
                count++;
            }
        }
        if (count == 0) {
            return;
        }
        final JiBingNvHuangZSParam param = passiveState.getParam(JiBingNvHuangZSParam.class);
        final long mpRecover = (long) (param.getMpRecoverWhenKillSummonUnit() * count);
        PassiveUtils.mpUpdate(context, damageReport, owner, owner, mpRecover, time, passiveState);
    }


    //释放大招时必然冰冻召唤物
    @Override
    public void attackEnd(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (!target.isSummon()) {
            return;
        }
        if (skillState.getType() != SkillType.SPACE) {
            return;
        }
        final JiBingNvHuangZSParam param = passiveState.getParam(JiBingNvHuangZSParam.class);
        final StateAddParam state = param.getState();
        PassiveUtils.addState(owner, target, state.getState(), state.getTime() + time, time, passiveState, context, skillReport);
    }
}
