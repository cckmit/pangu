package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.AddBuffWhenDamageParam;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.framework.utils.math.RandomUtils;
import com.pangu.framework.utils.reflect.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 当受伤时添加BUFF
 *
 * @author Kubby
 */
@Component
public class AddBuffWhenDamage implements DamagePassive {

    @Override
    public PassiveType getType() {
        return PassiveType.ADD_BUFF_WHEN_DAMAGE;
    }

    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time,
                       Context context, SkillState skillState, SkillReport skillReport) {
        AddBuffWhenDamageParam param = passiveState.getParam(AddBuffWhenDamageParam.class);

        if (!RandomUtils.isHit(param.getProb())) {
            return;
        }

        final String targetId = param.getTargetId();
        List<Unit> targets;
        if (StringUtils.isEmpty(targetId)) {
            targets = Collections.singletonList(owner);
        } else if (targetId.equals(AddBuffWhenDamageParam.MURDERER)) {
            targets = Collections.singletonList(attacker);
        } else {
            targets = TargetSelector.select(owner, targetId, time);
        }
        for (Unit target : targets) {
            for (String buffId : param.getBuffs()) {
                BuffFactory.addBuff(buffId, owner, target, time, skillReport, null);
            }
        }

        passiveState.addCD(time);
    }
}
