package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.UnitDiePassive;
import com.pangu.logic.module.battle.service.passive.param.ThunderHitParam;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import com.pangu.logic.module.battle.service.skill.param.StateAddParam;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 吉拉德挥舞他的武器，每4次攻击会对敌人造成雷电一击，造成眩晕效果
 * 如果此技能将目标击杀，则攻击下一个目标时会立刻再使用一次雷电一击
 */
@Component
public class ThunderHit implements AttackPassive, UnitDiePassive {
    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (skillState.getType() != SkillType.NORMAL) {
            return;
        }
        Integer chargedCount = passiveState.getAddition(Integer.class, 0);
        final ThunderHitParam param = passiveState.getParam(ThunderHitParam.class);
        if (chargedCount < param.getTriggerCount()) {
            //  正常普攻逐次充能
            passiveState.setAddition(++chargedCount);
            return;
        }
        //  重置充能
        passiveState.setAddition(0);
        //  标记目标
        context.addTag(target, PassiveType.THUNDER_HIT.name());

        //  造成伤害
        final StateAddParam stateAddParam = param.getStateAddParam();
        final PassiveValue passiveValue = PassiveValue.of(passiveState.getId(), owner.getId());
        final long addDmg = (long) (damage * param.getDmgUpRate());
        context.addPassiveValue(target, AlterType.HP, addDmg);
        passiveValue.add(Hp.of(addDmg));

        //  添加异常
        if (stateAddParam != null) {
            PassiveUtils.addState(owner, target, stateAddParam.getState(), time + stateAddParam.getTime(), time, context, passiveValue, skillReport);
        } else {
            skillReport.add(time, target.getId(), passiveValue);
        }
    }

    @Override
    public PassiveType getType() {
        return PassiveType.THUNDER_HIT;
    }

    //  使用雷霆一击击杀时，立刻充满能量
    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> dieUnits) {
        if (owner != attacker) {
            return;
        }
        final ThunderHitParam param = passiveState.getParam(ThunderHitParam.class);
        if (!param.isKillBonus()) {
            return;
        }
        for (Unit dieUnit : dieUnits) {
            if (context.hasTag(dieUnit, PassiveType.THUNDER_HIT.name())) {
                passiveState.setAddition(Integer.MAX_VALUE);
                break;
            }
        }
    }
}
