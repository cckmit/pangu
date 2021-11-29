package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.action.Action;
import com.pangu.logic.module.battle.service.action.EffectAction;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.*;
import com.pangu.logic.module.battle.service.passive.param.ShiKongXingZheZSParam;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import com.pangu.logic.module.battle.service.skill.utils.SkillUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

/**
 * 每次对敌人造成的伤害有10%将会通过时间存储起来，
 * 当累积的伤害会将敌人击杀时，
 * 来自时间下游的亚伯罕直接出现将其击杀(若击杀了敌方的亚伯罕则使自己拥有5秒的无敌)。
 * 每击杀一名英雄，亚伯罕获得500%攻击力的护盾
 */
@Component
public class ShiKongXingZheZS implements AttackPassive, UnitHpChangePassive, UnitDiePassive {
    private ShiKongXingZheZSParam getParam(PassiveState passiveState) {
        return passiveState.getParam(ShiKongXingZheZSParam.class);
    }

    //缓存伤害
    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final ShiKongXingZheZSParam param = getParam(passiveState);
        passiveState.setAddition(passiveState.getAddition(Long.class, 0L) + (long) (context.getHpChange(target) * param.getDmgDepositedRate()));
    }

    @Override
    public void hpChange(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> changeUnit) {
        //判断是否满足击杀条件
        final Long dmgAcc = passiveState.getAddition(Long.class, 0L);
        Unit target = null;
        for (Unit unit : changeUnit) {
            if (unit.isFriend(owner)) {
                continue;
            }
            final long actHp = context.getHpChange(unit) + unit.getValue(UnitValue.HP) + unit.getValue(UnitValue.SHIELD);
            if (actHp <= 0 || unit.isDead()) {
                continue;
            }
            if (dmgAcc + actHp <= 0) {
                target = unit;
                break;
            }
        }
        if (target == null) {
            return;
        }

        passiveState.setAddition(0L);
        //击杀行为延迟至下一帧执行，否则无法正确结算击杀来源
        final ShiKongXingZheZSParam param = getParam(passiveState);
        //道具消耗完毕后，提交一个延迟生成道具的行为
        final Unit targetToSlay = target;
        final Action itemGenAction = new Action() {
            private int actTime = time;

            @Override
            public int getTime() {
                return actTime;
            }

            @Override
            public void execute() {
                final SkillState skillState = SkillFactory.initState(param.getDeadlySkill());
                final Unit target = owner.getTarget();
                SkillReport skillReport = SkillReport.sing(actTime, owner.getId(), skillState.getId(), skillState.getSingTime(), target != null && !target.isDead() ? target.getId() : null);
                owner.getBattle().addReport(skillReport);
                final int effectTime = actTime + skillState.getSingTime();
                final EffectAction effectAction = new EffectAction(effectTime, owner, skillState, skillReport, skillState.getEffectStates().get(0), Collections.singletonList(targetToSlay));
                owner.addTimedAction(effectAction);
            }
        };
        owner.addTimedAction(itemGenAction);
    }

    //击杀奖励
    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> dieUnits) {
        if (attacker != owner) {
            return;
        }

        int killedEnemyHeroCount = 0;
        int killedShiKongXingZhe = 0;
        for (Unit dieUnit : dieUnits) {
            if (dieUnit.isSummon()) {
                continue;
            }
            if (dieUnit.isFriend(owner)) {
                continue;
            }
            killedEnemyHeroCount++;

            if (owner.getNormalSkill().equals(dieUnit.getNormalSkill())) {
                killedShiKongXingZhe++;
            }
        }

        final ShiKongXingZheZSParam param = getParam(passiveState);

        if (killedShiKongXingZhe > 0) {
            SkillUtils.addState(owner, owner, UnitState.WU_DI, time, time + param.getWudiDur(), damageReport, context);
        }

        final double atkFactor = param.getAtkFactor();
        if (atkFactor <= 0) {
            return;
        }

        final long shieldVal = (long) (killedEnemyHeroCount * owner.getHighestATK() * atkFactor);
        context.addPassiveValue(owner, AlterType.SHIELD_UPDATE, shieldVal);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.SHI_KONG_XING_ZHE_ZS;
    }
}
