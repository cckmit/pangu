package com.pangu.logic.module.battle.service.action.custom;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.service.action.Action;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import com.pangu.logic.module.battle.service.skill.param.GuWangZhiNuParam;
import lombok.Getter;

import java.util.List;

/**
 * 骨王之怒
 * 数秒蓄力，蓄力期间降低受到的伤害并免疫控制，蓄力结束时攻击面前的所有敌人造成140%攻击力的
 * 伤害，该攻击额外造成蓄力期间自己所受伤害的200%。
 * 2级:伤害提升至160%攻击力
 * 3级:自身回复造成伤害的40%血量。
 * 4级:伤害提升至180%攻击力
 */
@Getter
public class GuWangZhiNuAction implements Action {
    private final int time;
    private final Unit owner;
    private final SkillState skillState;
    private final EffectState effectState;
    private final SkillReport skillReport;

    public GuWangZhiNuAction(int time, Unit owner, SkillState skillState, EffectState effectState, SkillReport skillReport) {
        this.time = time;
        this.owner = owner;
        this.skillState = skillState;
        this.effectState = effectState;
        this.skillReport = skillReport;
    }

    @Override
    public void execute() {
        // 使用被动来累计收到的伤害
        GuWangZhiNuParam param = effectState.getParam(GuWangZhiNuParam.class);
        String passiveId = param.getPassive();
        PassiveState passiveStates = owner.getPassiveStates(passiveId);
        long gotDamages = 0;
        if (passiveStates.getAddition(Long.class) != null) {
            gotDamages = passiveStates.getAddition(Long.class);
        }
        owner.removePassive(passiveStates);
        // 自身回复血量,移除霸体状态
        owner.removeState(UnitState.BA_TI, skillState.getSingAfterDelay());
        Context context = new Context(owner);
        // 恢复生命
        if (gotDamages < 0 && param.getDamageToHpRate() > 0) {
            long recoverHp = (long) (-gotDamages * param.getDamageToHpRate());
            if (recoverHp > 0) {
                recoverHp = (long) Math.min(owner.getValue(UnitValue.HP_MAX) * param.getDamageToHpRate(), recoverHp);
                skillReport.add(time, owner.getId(), Hp.of(recoverHp));
                context.addValue(owner, AlterType.HP, recoverHp);
            }
        }

        long damageEnhance = (long) (gotDamages * param.getDamageReturnRate());

        String selectId = param.getTargetId();
        List<Unit> units = TargetSelector.select(owner, selectId, time);
        SkillEffect skillEffect = SkillFactory.getSkillEffect(EffectType.HP_P_DAMAGE);
        DamageParam damageParam = new DamageParam(param.getFactor());
        effectState.setParamOverride(damageParam);
        for (Unit unit : units) {
            context.execAttackBeforePassiveAndEffect(time, skillEffect, unit, skillState, effectState, skillReport);

            // 将受到的伤害反伤回去
            if (gotDamages < 0) {
                damageEnhance = (long) Math.max(-owner.getValue(UnitValue.HP_MAX) * param.getDamageToHpRate(), damageEnhance) / units.size();
                skillReport.add(time, unit.getId(), Hp.of(damageEnhance));
                context.addValue(unit, AlterType.HP, damageEnhance);
            }
        }
        effectState.setParamOverride(null);
        context.execute(time, skillState, skillReport);
    }
}
