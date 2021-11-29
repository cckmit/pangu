package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.RecoverPassive;
import com.pangu.logic.module.battle.service.passive.param.BuffAddWhenRecoveredParam;
import com.pangu.framework.utils.math.RandomUtils;
import org.springframework.stereotype.Component;

/**
 * 受到治疗时有概率为自己添加buff
 */
@Component
public class BuffAddWhenRecovered implements RecoverPassive {
    @Override
    public PassiveType getType() {
        return PassiveType.BUFF_ADD_WHEN_RECOVERED;
    }

    @Override
    public void recover(PassiveState passiveState, Unit owner, Unit from, long recover, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final BuffAddWhenRecoveredParam param = passiveState.getParam(BuffAddWhenRecoveredParam.class);
        if (!RandomUtils.isHit(param.getTriggerRate())) {
            return;
        }

        BuffFactory.addBuff(param.getBuffId(), owner, owner, time, skillReport, null);
        passiveState.addCD(time);
    }
}
