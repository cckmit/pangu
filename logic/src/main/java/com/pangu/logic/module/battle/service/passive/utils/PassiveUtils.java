package com.pangu.logic.module.battle.service.passive.utils;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.values.*;
import com.pangu.logic.module.battle.service.alter.MpAlter;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.skill.effect.HpMagicDamage;
import com.pangu.logic.module.battle.service.skill.effect.HpPhysicsDamage;

/**
 * 更新上下文并打印战报工具类
 */
public class PassiveUtils {
    //修改战报
    public static void hpUpdate(Context context, ITimedDamageReport timedDamageReport, Unit target, long dmgChange, int time) {
        context.addPassiveValue(target, AlterType.HP, dmgChange);
        SkillReportEditor.editHpDamageReport(timedDamageReport, target, dmgChange, time);
    }

    //独立战报
    public static void hpUpdate(Context context, ITimedDamageReport timedDamageReport, Unit owner, Unit target, long dmgChange, int time, PassiveState passiveState) {
        context.addPassiveValue(target, AlterType.HP, dmgChange);
        timedDamageReport.add(time, target.getId(), PassiveValue.single(passiveState.getId(), owner.getId(), Hp.of(dmgChange)));
    }

    //魔法伤害通用方法
    public static void hpMagicDamage(HpMagicDamage damageEffect, Unit owner, Unit target, SkillState skillState, EffectState effectState, int time, Context context, ITimedDamageReport timedDamageReport, PassiveState passiveState) {
        final HpMagicDamage.MagicDamageCalcResult magicDamageCalcResult = damageEffect.calcDamage(owner, target, skillState, effectState, time);
        final long damage = magicDamageCalcResult.getDamage();
        final boolean isCrit = magicDamageCalcResult.isCrit();

        context.addPassiveValue(target, AlterType.HP, damage);
        context.setCrit(target, isCrit);
        context.setMagic(target);
        timedDamageReport.add(time, target.getId(), PassiveValue.single(passiveState.getId(), owner.getId(), new Hp(damage, isCrit, false)));
    }

    //物理伤害通用方法
    public static void hpPhysicsDamage(HpPhysicsDamage damageEffect, Unit owner, Unit target, SkillState skillState, EffectState effectState, int time, Context context, ITimedDamageReport timedDamageReport, PassiveState passiveState) {
        final HpPhysicsDamage.PhysicsDamageCalcResult physicsDamageCalcResult = damageEffect.calcDamage(owner, target, skillState, effectState, time);
        final long damage = physicsDamageCalcResult.getDamage();
        final boolean isCrit = physicsDamageCalcResult.isCrit();
        final boolean isBlock = physicsDamageCalcResult.isBlock();

        context.addPassiveValue(target, AlterType.HP, damage);
        context.setCrit(target, isCrit);
        timedDamageReport.add(time, target.getId(), PassiveValue.single(passiveState.getId(), owner.getId(), new Hp(damage, isCrit, isBlock)));
    }


    //修改战报
    public static void mpUpdate(Context context, ITimedDamageReport timedDamageReport, Unit target, long mpChange, int time) {
        final long mpChangeForReport = MpAlter.calMpChange(target, mpChange);
        if (mpChangeForReport == 0) {
            return;
        }
        context.addPassiveValue(target, AlterType.MP, mpChange);
        SkillReportEditor.editMpChangeReport(timedDamageReport, target, mpChangeForReport, time);
    }

    //独立战报
    public static void mpUpdate(Context context, ITimedDamageReport timedDamageReport, Unit owner, Unit target, long mpChange, int time, PassiveState passiveState) {
        final long mpChangeForReport = MpAlter.calMpChange(target, mpChange);
        if (mpChangeForReport == 0) {
            return;
        }
        context.addPassiveValue(target, AlterType.MP, mpChange);
        timedDamageReport.add(time, target.getId(), PassiveValue.single(passiveState.getId(), owner.getId(), new Mp(mpChangeForReport)));
    }

    /**
     * @param owner        添加异常的被动的持有者
     * @param unit         被添加异常的目标
     * @param state        异常状态
     * @param validTime    过期时刻
     * @param time         当前时刻
     * @param passiveState 被动状态
     * @param context      上下文
     * @param damageReport 战报
     * @return 异常状态是否添加成功
     * <p>
     * 此方法仅适用于在【被动】效果中添加异常状态。
     * 在【主动】效果和【BUFF】中添加异常请使用：
     * @see com.pangu.logic.module.battle.service.skill.utils.SkillUtils#addState(Unit, Unit, UnitState, int, int, ITimedDamageReport, Context)
     */
    public static boolean addState(Unit owner, Unit unit, UnitState state, int validTime, int time, PassiveState passiveState, Context context, ITimedDamageReport damageReport) {
        final int trueValidTime = context.addState(owner, unit, state, time, validTime, damageReport);
        if (trueValidTime <= time) {
            damageReport.add(time, unit.getId(), new Immune());
            return false;
        }
        damageReport.add(time, unit.getId(), PassiveValue.single(passiveState.getId(), owner.getId(), new StateAdd(state, trueValidTime)));
        return true;
    }

    /**
     * @param caster
     * @param unit         被添加异常的目标
     * @param state        异常状态
     * @param validTime    过期时刻
     * @param time         当前时刻
     * @param context      上下文
     * @param passiveValue 被动战报
     * @param damageReport 战报
     * @return 异常状态是否添加成功
     * <p>
     * <p>
     * 此方法仅适用于在【被动】效果中添加异常状态并【合并被动战报】的情况
     */
    public static boolean addState(Unit caster, Unit unit, UnitState state, int validTime, int time, Context context, PassiveValue passiveValue, ITimedDamageReport damageReport) {
        final int trueValidTime = context.addState(caster, unit, state, time, validTime, damageReport);
        if (trueValidTime <= time) {
            damageReport.add(time, unit.getId(), new Immune());
            return false;
        }
        passiveValue.add(new StateAdd(state, trueValidTime));
        damageReport.add(time, unit.getId(), passiveValue);
        return true;
    }

}
