package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import com.pangu.logic.module.battle.service.skill.param.DianCiZhaDanParam;
import com.pangu.logic.module.battle.service.skill.param.StateAddParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 电磁炸弹:
 * 每次普通攻击都会有20%的几率将下次普攻替换为一颗炸弹，扔向目标炸弹会在3秒后爆炸,造成范围魔法伤害130%
 * 2级:伤害提升至145%
 * 3级:如果炸弹附着的敌军身上有时光发条,则受到爆炸伤害的所有敌军都会造成2秒的封印(无法释放技能,无法触发被动)
 * 4级:每次攻击不触发时,下一次的攻击概率提升15%,直到触发之后重置
 */
@Component
public class DianCiZhaDan implements SkillEffect {
    @Autowired
    private HpMagicDamage hpMagicDamage;
    @Autowired
    private StateAddEffect stateAddEffect;

    @Override
    public EffectType getType() {
        return EffectType.DIAN_CI_ZHA_DAN;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final DianCiZhaDanParam param = state.getParam(DianCiZhaDanParam.class);
        //获取以自身为参照的目标
        final List<Unit> currentTargets = TargetSelector.select(target, param.getTargetId(), time);

        //构建魔伤效果上下文
        final DamageParam damageParam = new DamageParam();
        damageParam.setFactor(param.getFactor());
        state.setParamOverride(damageParam);
        //执行魔伤
        for (Unit unit : currentTargets) {
            hpMagicDamage.execute(state, owner, unit, skillReport, time, skillState, context);
        }
        state.setParamOverride(null);

        //未配置异常 | 炸弹附着单位身上不存在指定tag的buff，不执行添加异常的逻辑
        if (param.getUnitState() == null || target.getBuffStateByTag(param.getConditionTag()) == null) return ;
        //构建添加异常上下文
        final StateAddParam stateAddParam = new StateAddParam();
        stateAddParam.setState(param.getUnitState());
        stateAddParam.setTime(param.getDuration());
        state.setParamOverride(stateAddParam);
        //执行异常添加
        for (Unit currentTarget : currentTargets) {
            stateAddEffect.execute(state, owner, currentTarget, skillReport, time, skillState, context);
        }
        state.setParamOverride(null);
    }
}
