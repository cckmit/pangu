package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.action.AddBuffAction;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillReleasePassive;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * 灵魂收割者·路西法专属装备
 * 1：英雄自身布阵前排时，每个后排存活友军会提升自身5%免伤，布阵后排时候，每个前排存活友军会提升自身10%爆伤
 * 10：英雄自身布阵前排时，每个后排存活友军会提升自身8%免伤，布阵后排时候，每个前排存活友军会提升自身15%爆伤
 * 20：使用大招后的6秒内，同时获得布阵前排和后排的效果
 * 30：英雄自身布阵前排时，每个后排存活友军会提升自身12%免伤，布阵后排时候，每个前排存活友军会提升自身25%爆伤
 *
 * @author Kubby
 */
@Component
public class LingHunShouGeZheZS implements SkillReleasePassive {

    @Override
    public PassiveType getType() {
        return PassiveType.LING_HUN_SHOU_GE_ZHE_ZS;
    }

    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time,
                             Context context, SkillReport skillReport) {
        if (owner != attacker) {
            return;
        }
        if (skillState.getType() != SkillType.SPACE) {
            return;
        }
        String buffId = passiveState.getParam(String.class);
        AddBuffAction action = AddBuffAction.of(owner, owner, Collections.singletonList(buffId), true, time, skillReport);
        owner.addTimedAction(action);
    }

}
