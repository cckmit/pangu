package com.pangu.logic.module.battle.service.skill;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;

/**
 * 技能效果接口
 */
public interface SkillEffect {

    //  获取效果类型
    EffectType getType();

    //  运算技能带来的战斗单元数值变更
    void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context);

}
