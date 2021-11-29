package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.DmgUpByTargetBuffCountParam;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 如果目标身上每有一层出血，绝命猎舞这个技能就会造成额外的20%伤害。至多可达100%伤害
 */
@Component
public class DmgUpByTargetBuffCount implements AttackPassive {
    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final DmgUpByTargetBuffCountParam param = passiveState.getParam(DmgUpByTargetBuffCountParam.class);
        if (!skillState.getTag().equals(param.getTriggerTag())) {
            return;
        }

        final List<BuffState> buffs = target.getBuffByClassify(param.getBuffClassify());
        if (buffs.isEmpty()) {
            return;
        }

        final double totalDmgUpRate = Math.min(param.getMaxDmgUpRate(), buffs.size() * param.getDmgUpRatePerBuffCount());
        PassiveUtils.hpUpdate(context, skillReport, owner, target, (long) (totalDmgUpRate * damage), time, passiveState);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.DMG_UP_ON_TARGET_BUFF_COUNT;
    }
}
