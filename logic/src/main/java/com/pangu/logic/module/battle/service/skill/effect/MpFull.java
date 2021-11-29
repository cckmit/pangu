package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.AlterAfterValue;
import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Mp;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * 能量回满（立即生效）
 * @author Kubby
 */
@Component
public class MpFull implements SkillEffect {

    @Override
    public EffectType getType() {
        return EffectType.MP_FULL;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time,
                        SkillState skillState, Context context) {
        long recoverMp = owner.getValue(UnitValue.MP_MAX) - owner.getValue(UnitValue.MP);
        if (recoverMp > 0) {
            long curMp = owner.increaseValue(UnitValue.MP, recoverMp);
            AlterAfterValue afterValue = new AlterAfterValue();
            afterValue.put(UnitValue.MP, curMp);
            skillReport.addAfterValue(time, Collections.singletonMap(owner.getId(), afterValue));
            skillReport.add(time, owner.getId(), new Mp(recoverMp));
        }
    }
}
