package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.BeAttackBeforePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.EffectOnDodgeParam;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.skill.effect.HpHigherDamage;
import com.pangu.framework.utils.reflect.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 在战斗中每次闪避后，将永久获得2%的攻击力，至多50层在战斗中每次闪避后，将永久获得2%的攻击力，至多50层
 */
@Component
public class EffectOnDodge implements BeAttackBeforePassive {
    @Autowired
    private HpHigherDamage higherDamage;

    @Override
    public void beAttackBefore(PassiveState passiveState, EffectState effectState, Unit owner, Unit attacker, int time, Context context, SkillReport skillReport) {

    }

    @Override
    public void beAttackEnd(PassiveState passiveState, EffectState effectState, Unit owner, Unit attacker, int time, Context context, SkillReport skillReport) {
        //  未成功闪避
        if (!context.isMiss(owner)) {
            return;
        }

        //  选择影响目标
        final EffectOnDodgeParam param = passiveState.getParam(EffectOnDodgeParam.class);
        final String target = param.getTarget();
        List<Unit> targets;
        if (StringUtils.isEmpty(target)) {
            targets = Collections.singletonList(attacker);
        } else {
            targets = TargetSelector.select(owner, target, time);
        }

        //  对影响目标执行一系列操作
        final String psvId = passiveState.getId();
        for (Unit unit : targets) {
            //  修改数值
            context.modVal(owner, unit, time, skillReport, param.getValModParam(), psvId, passiveState.getCaster());

            //  伤害
            context.passiveAtkDmg(owner, unit, time, skillReport, higherDamage, param.getDmgParam(), psvId, passiveState.getCaster());
        }

        passiveState.addCD(time);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.EFFECT_ON_DODGE;
    }
}
