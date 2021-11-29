package com.pangu.logic.module.battle.service.select;

import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SelectResult {

    // 生效技能
    private SkillState skillState;

    // 技能目标
    private List<Unit> targets;
}
