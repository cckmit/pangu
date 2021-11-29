package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.*;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Mp;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.*;
import com.pangu.logic.module.battle.service.passive.param.YuXueParam;
import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class YiJiZhiMing implements AttackBeforePassive, UnitDiePassive, SkillReleasePassive {

    //不可闪避，血线低于某个阈值时必定暴击
    @Override
    public void attackBefore(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time, Context context, SkillReport skillReport) {
        //释放技能为大招时才执行逻辑
        final Addition addition = getAddition(passiveState);
        final SkillState skillState = addition.skillState;
        if (skillState == null || skillState.getType() != SkillType.SPACE) return;
        //缓存角色原始命中率
        addition.originHit = owner.getRate(UnitRate.HIT);
        //修改角色原始命中率
        final YuXueParam param = passiveState.getParam(YuXueParam.class);
        owner.setRate(UnitRate.HIT, 2);

        //目标血线不满足阈值条件不执行逻辑
        if (param.getRecoverHpByAttackRate() <= ((double) target.getValue(UnitValue.HP) / target.getValue(UnitValue.HP_MAX)))
            return;
        //直接修改修改伤害效果参数中的暴击表达式，此表达式优先级高于正常暴击计算公式
        for (EffectState effect : skillState.getEffectStates()) {
            if (effect.getType() == EffectType.HP_M_DAMAGE || effect.getType() == EffectType.HP_P_DAMAGE) {
                final DamageParam originParam = effect.getParam(DamageParam.class);
                final DamageParam overrideParam = new DamageParam();
                BeanUtils.copyProperties(originParam, overrideParam);
                overrideParam.setCritExp("1>0");
                effect.setParamOverride(overrideParam);
            }
        }
    }

    @Override
    public void attackEnd(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time, Context context, SkillReport skillReport) {
        //释放技能为大招时才执行逻辑
        final Addition addition = getAddition(passiveState);
        if (addition.skillState == null || addition.skillState.getType() != SkillType.SPACE) return;
        //从缓存中恢复角色原始数值
        owner.setRate(UnitRate.HIT, addition.originHit);
        effectState.setParamOverride(null);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.YI_JI_ZHI_MING;
    }

    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> dieUnits) {
        final Addition addition = getAddition(passiveState);
        final SkillState skillState = addition.skillState;
        //自身以大招击杀时才恢复能量
        if (owner != attacker || skillState == null || skillState.getType() != SkillType.SPACE) return;
        addition.skillState = null;
        //MP恢复
        final YuXueParam param = passiveState.getParam(YuXueParam.class);
        if (param.getAddMp() <= 0) {
            return;
        }
        PassiveValue passiveValue = PassiveValue.of(passiveState.getId(), owner.getId());
        context.addPassiveValue(owner, AlterType.MP, param.getAddMp());
        passiveValue.add(new Mp(param.getAddMp()));
    }

    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time, Context context, SkillReport skillReport) {
        final Addition addition = getAddition(passiveState);
        addition.skillState = skillState;
    }

    private Addition getAddition(PassiveState passiveState) {
        Addition addition = passiveState.getAddition(Addition.class);
        if (addition == null) {
            addition = new Addition();
            passiveState.setAddition(addition);
        }
        return addition;
    }

    private static class Addition {
        //最近释放的技能
        private SkillState skillState;
        //原始命中率
        private double originHit;
    }
}
