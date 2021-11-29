package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import com.pangu.logic.module.battle.service.skill.param.FengZhiXiDamageParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 斩风之息·武技能：掠风斩命
 * 4级:如果释放该技能时，自己身上有2层或2层以上的风之息效果,则会将目标周围的敌人全部击飞，并造成一半的技能伤害。同时重置风之息效果
 * @author Kubby
 */
@Component
public class FengZhiXiDamage implements SkillEffect {

    @Autowired
    private HpPhysicsDamage physicsDamage;

    @Override
    public EffectType getType() {
        return EffectType.FENG_ZHI_XI_DAMAGE;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time,
                        SkillState skillState, Context context) {
        FengZhiXiDamageParam param = state.getParam(FengZhiXiDamageParam.class);

        List<BuffState> buffStates = owner.getBuffBySettingId(param.getBuffId());
        if (buffStates.size() < param.getTriggerCount()) {
            return;
        }

        List<Unit> rounds = TargetSelector.select(target, param.getTargetRoundSelectId(), time);
        rounds.remove(target);
        if (rounds.isEmpty()) {
            return;
        }

        EffectState effectState = new EffectState(null, 0);
        effectState.setParamOverride(new DamageParam(param.getFactor()));

        /* 范围伤害 */
        for (Unit round : rounds) {
            physicsDamage.execute(effectState, owner, round, skillReport, time, skillState, context);
            /* 击飞 */
            round.addState(UnitState.DISABLE, time + param.getJifeiTime());
        }

        /* 重置风之息BUFF */
        BuffFactory.removeBuffStates(buffStates, owner, time);
    }
}
