package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.RecoverTargetPassive;
import org.springframework.stereotype.Component;

/**
 * 唤星女神·维纳斯技能：星环
 * 1级：每次造成治疗时,会提升目标10%攻击力,持续5秒
 * 2级：提升10%的魔防
 * 3级：获得8%的抗暴率
 * 4级：免伤率增加15%
 * @author Kubby
 */
@Component
public class XingHuan implements RecoverTargetPassive {

    @Override
    public PassiveType getType() {
        return PassiveType.XING_HUAN;
    }

    @Override
    public void recoverTarget(PassiveState passiveState, Unit owner, Unit target, long recover, int time,
                              Context context, SkillState skillState, SkillReport skillReport) {
        String[] params = passiveState.getParam(String[].class);
        for (String buffId : params) {
            BuffFactory.addBuff(buffId, owner, target, time, skillReport, null);
        }
    }
}
