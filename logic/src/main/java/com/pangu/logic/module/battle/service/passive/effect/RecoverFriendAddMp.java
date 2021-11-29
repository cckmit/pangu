package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Mp;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.RecoverTargetPassive;
import com.pangu.logic.module.battle.service.passive.param.RecoverFriendAddMpParam;
import org.springframework.stereotype.Component;

/**
 * 治疗友方单元添加其MP
 */
@Component
public class RecoverFriendAddMp implements RecoverTargetPassive {
    @Override
    public void recoverTarget(PassiveState passiveState, Unit owner, Unit target, long recover, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (owner.getFriend() != target.getFriend()) {
            return;
        }
        final RecoverFriendAddMpParam param = passiveState.getParam(RecoverFriendAddMpParam.class);
        final long currentMp = target.getValue(UnitValue.MP);
        if (currentMp > param.getLessMp()) {
            return;
        }
        final int addMp = param.getAddMp();
        context.addPassiveValue(target, AlterType.MP, addMp);
        skillReport.add(time, target.getId(), new Mp(addMp));
        passiveState.addCD(time);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.RECOVER_FRIEND_ADD_MP;
    }
}
