package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.*;
import com.pangu.logic.module.battle.service.passive.param.ShengGuangXiLiZSParam;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * 1：受到圣光之耀的队友和自身参与击杀敌方英雄时，自身的下次普攻变为释放一次圣光冲击，仅造成常规圣光攻击的55%伤害
 * 10：仅造成常规圣光攻击的70%伤害
 * 20：仅造成常规圣光攻击的85%伤害
 * 30：每次触发自身额外回能200
 */
@Component
public class ShengGuangXiLiZS implements SkillReleasePassive, SkillSelectPassive, UnitDiePassive, AttackPassive, UnitHpChangePassive {
    @Override
    public PassiveType getType() {
        return PassiveType.SHENG_GUANG_XI_LI_ZS;
    }

    //释放替换普攻时回复能量
    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time, Context context, SkillReport skillReport) {
        if (!skillState.getTag().equals("sheng_guang_chong_ji_zs")) {
            return;
        }
        final ShengGuangXiLiZSParam param = getParam(passiveState);
        PassiveUtils.mpUpdate(context, skillReport, owner, owner, param.getMp(), time, passiveState);
    }

    //调整替换技能的伤害
    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (!skillState.getTag().equals("sheng_guang_chong_ji_zs")) {
            return;
        }
        final double dmgFactor = getParam(passiveState).getDmgFactor();
        final double dmgDecr = -damage * (1 - dmgFactor);
        PassiveUtils.hpUpdate(context, skillReport, target, (long) dmgDecr, time);
    }

    //符合条件时将普攻替换为技能
    @Override
    public SkillState skillSelect(PassiveState passiveState, SkillState skillState, Unit owner, int time) {
        //非普攻不触发
        if (skillState.getType() != SkillType.NORMAL) {
            return null;
        }
        //未达成条件不触发
        if (!getAddition(passiveState).triggered) {
            return null;
        } else {
            getAddition(passiveState).triggered = false;
        }
        return SkillFactory.initState(getParam(passiveState).getSkillId());
    }


    //符合条件的角色达成击杀时，回复能量
    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> dieUnits) {
        //攻击者不是友方，不执行
        if (!owner.getFriend().getAllUnit().contains(attacker)) {
            return;
        }

        final Addition addition = getAddition(passiveState);
        final Map<Unit, Set<Unit>> dead2Killers = addition.dead2Killers;
        //参与击杀者身上不存在圣光之耀，不执行
        for (Unit dieUnit : dieUnits) {
            if (owner.isFriend(dieUnit)) {
                continue;
            }
            if (CollectionUtils.isEmpty(dead2Killers.get(dieUnit))) {
                continue;
            }
            addition.triggered = true;
            break;
        }
    }

    //记录参与击杀的目标
    @Override
    public void hpChange(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> changeUnit) {
        if (attacker.getBuffStateByTag("sheng_guang_zhi_yao") == null) {
            return;
        }
        final Addition addition = getAddition(passiveState);
        for (Unit hpChangeUnit : changeUnit) {
            final long totalDmg = context.getTotalHpChange(hpChangeUnit);
            if (totalDmg >= 0) {
                continue;
            }
            final Set<Unit> killers = addition.dead2Killers.computeIfAbsent(hpChangeUnit, k -> new HashSet<>(2));
            killers.add(attacker);
        }
    }


    private static class Addition {
        private boolean triggered;
        private Map<Unit, Set<Unit>> dead2Killers = new HashMap<>(6);
    }

    private Addition getAddition(PassiveState passiveState) {
        Addition addition = passiveState.getAddition(Addition.class);
        if (addition == null) {
            addition = new Addition();
            passiveState.setAddition(addition);
        }
        return addition;
    }

    private ShengGuangXiLiZSParam getParam(PassiveState passiveState) {
        return passiveState.getParam(ShengGuangXiLiZSParam.class);
    }
}
