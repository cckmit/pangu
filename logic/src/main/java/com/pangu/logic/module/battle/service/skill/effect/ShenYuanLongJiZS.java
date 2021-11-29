package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.action.custom.ShenYuanLongJiZSAction;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 深渊龙姬·安吉丽娜专属装备
 * 1：龙形态下，震慑所有敌人，敌人每秒降低20能量
 * 10：敌人每秒降低30能量
 * 20：敌人每秒降低40能量
 * 30：敌人每秒降低50能量
 *
 * @author Kubby
 */
@Component
public class ShenYuanLongJiZS implements SkillEffect {

    @Override
    public EffectType getType() {
        return EffectType.SHEN_YUAN_LONG_JI_ZS;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time,
                        SkillState skillState, Context context) {
        state.setAddition(new ShenYuanLongJiZS.ShenYuanLongJiZSAddition());
        ShenYuanLongJiZSAction action = ShenYuanLongJiZSAction.of(time, owner, state, skillReport);
        owner.getBattle().addWorldAction(action);
    }

    @Getter
    public class ShenYuanLongJiZSAddition {

        private Map<Unit, BuffState> buffs = new HashMap<>();

        public boolean hasBuff(Unit unit) {
            return buffs.containsKey(unit);
        }

        public void addBuff(Unit unit, BuffState buffState) {
            buffs.put(unit, buffState);
        }

        public void clearBuff() {
            buffs.clear();
        }
    }
}
