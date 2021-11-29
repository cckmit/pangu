package com.pangu.logic.module.battle.service.action.custom;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.SummonRemove;
import com.pangu.logic.module.battle.service.action.Action;
import com.pangu.logic.module.battle.service.core.Unit;
import lombok.Getter;

import java.util.Collections;

// 移除技能召唤的战斗单元
@Getter
public class SummonRemoveAction implements Action {

    // 执行时间
    private final int time;

    // 需要移除的单元
    private final Unit unit;

    // 技能战报
    private final SkillReport skillReport;

    public SummonRemoveAction(int time, Unit unit, SkillReport skillReport) {
        this.time = time;
        this.unit = unit;
        this.skillReport = skillReport;
    }

    @Override
    public void execute() {
        if (unit.isDead()) {
            return;
        }
        unit.dead();
        skillReport.add(time, unit.getSummonUnit().getId(), new SummonRemove(Collections.singletonList(unit.getId())));
    }
}
