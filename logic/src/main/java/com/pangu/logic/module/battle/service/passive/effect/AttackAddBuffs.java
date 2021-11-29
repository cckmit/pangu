package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.AttackAddBuffsParam;
import com.pangu.framework.utils.math.RandomUtils;
import org.springframework.stereotype.Component;

@Component
public class AttackAddBuffs implements AttackPassive {
    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final AttackAddBuffsParam param = passiveState.getParam(AttackAddBuffsParam.class);
        final SkillType type = param.getType();
        if (type != null && type != skillState.getType()) {
            return;
        }
        double rate = param.getRate();
        if (rate > 0 && rate < 1 && !RandomUtils.isHit(rate)) {
            return;
        }
        for (String buff : param.getBuffs()) {
            BuffFactory.addBuff(buff, owner, target, time, skillReport, null);
        }
        passiveState.addCD(time);
        skillReport.add(time, target.getId(), PassiveValue.of(passiveState.getId(), owner.getId()));
    }

    @Override
    public PassiveType getType() {
        return PassiveType.ATTACK_ADD_BUFFS;
    }
}
