package com.pangu.logic.module.battle.service.action;

import com.pangu.logic.module.battle.model.*;
import com.pangu.logic.module.battle.model.report.MpFrom;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Mp;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import lombok.Getter;

import java.util.Collections;

/**
 * 技能行动
 */
@Getter
public class SkillAction implements Action {
    private final int time;
    private final Unit owner;
    private final SkillState skillState;
    private final SkillReport skillReport;
    private final int singAfterDelay;

    public SkillAction(int time, Unit owner, SkillState skillState, SkillReport skillReport, int singAfterDelay) {
        this.time = time;
        this.owner = owner;
        this.skillState = skillState;
        this.skillReport = skillReport;
        this.singAfterDelay = singAfterDelay;
    }

    @Override
    public void execute() {
        if (owner.isDead()) {
            return;
        }
        // 技能冷却以及扣减怒气
        skillState.calCd(time, singAfterDelay);
        // 释放技能怒气修改
        int mpChange = skillState.getMp();
        // MP变化修正
        mpChange = mpModifier(mpChange);
        if (mpChange != 0) {
            long curMp = owner.increaseValue(UnitValue.MP, mpChange);
            AlterAfterValue afterValue = new AlterAfterValue();
            afterValue.put(UnitValue.MP, curMp);
            skillReport.addAfterValue(time, Collections.singletonMap(owner.getId(), afterValue));
            skillReport.add(time, owner.getId(), new Mp(mpChange, MpFrom.SKILL));
        }

        // 重新回到初始行动
        owner.reset(time + singAfterDelay);

        // 使用单独的行动队列执行技能效果
        SkillEffectAction skillEffectAction = new SkillEffectAction(time, owner, skillState, skillReport);
        skillEffectAction.execute();

    }

    /**
     * 打断技能
     */
    public void broken(int time) {
        skillReport.broken(time);
        owner.reset(time);
    }

    private int mpModifier(int originMPChange) {
        if (skillState.getType() != SkillType.NORMAL) {
            return originMPChange;
        }
        originMPChange += owner.getValue(UnitValue.NORMAL_MP_ADD);
        if (originMPChange < 0) {
            return 0;
        }
        double normalMPDerate = owner.getRate(UnitRate.NORMAL_MP_ADD) + owner.getRate(UnitRate.MP_ADD_RATE);
        originMPChange = (int) (originMPChange * (1 + normalMPDerate));
        if (originMPChange < 0) {
            originMPChange = 0;
        }
        return originMPChange;
    }
}
