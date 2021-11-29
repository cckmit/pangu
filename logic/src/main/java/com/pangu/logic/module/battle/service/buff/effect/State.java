package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.report.BuffReport;
import com.pangu.logic.module.battle.service.buff.Buff;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.utils.SkillUtils;
import com.pangu.logic.module.common.resource.Formula;
import com.pangu.framework.resource.Storage;
import com.pangu.framework.resource.anno.Static;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 添加状态buff
 * 默认情况，不应该走到这里，应该使用技能效果处理
 */
@Component
@Slf4j
public class State implements Buff {

    @Static
    private Storage<String, Formula> formulaStorage;

    @Override
    public BuffType getType() {
        return BuffType.STATE;
    }

    @Override
    public boolean add(BuffState state, Unit unit, int time) {
        UnitState type = state.getParam(UnitState.class);
        if (type.immune != null && type.immune.length != 0) {
            for (UnitState immu : type.immune) {
                if (unit.hasState(immu, time)) {
                    return false;
                }
            }
        }
        unit.addBuff(state);
        BuffReport buffReport = state.getBuffReport();
        final Unit caster = state.getCaster();
        Context context = new Context(caster);
        SkillUtils.addState(caster, unit, type, time, time + state.getTime(), buffReport, context);
        context.execute(time, buffReport);
        return true;
    }

    @Override
    public void update(BuffState state, Unit unit, int time, Object o) {

    }

    @Override
    public void remove(BuffState state, Unit unit, int time) {
        unit.removeBuff(state);
    }
}
