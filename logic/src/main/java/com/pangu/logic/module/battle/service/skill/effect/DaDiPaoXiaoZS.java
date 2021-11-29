package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.action.custom.DaDiPaoXiaoZSAction;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.LinkedList;

/**
 * 大地咆哮·塔巴斯专属装备
 * 1：战斗中，身边每有一个友军，自身攻击力+3%
 * 10：战斗中，身边每有一个友军，自身攻击力+5%
 * 20：战斗中，身边每有一个友军，自身攻击力+8%
 * 30：当场上敌友军总量大于等于5的时候，自身每秒回复5%生命值，并免控
 * @author Kubby
 */
@Component
public class DaDiPaoXiaoZS implements SkillEffect {

    @Override
    public EffectType getType() {
        return EffectType.DA_DI_PAO_XIAO_ZS;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time,
                        SkillState skillState, Context context) {
        state.setAddition(new DaDiPaoXiaoZSAddition());
        DaDiPaoXiaoZSAction action = DaDiPaoXiaoZSAction.of(time, owner, state, skillReport);
        owner.addTimedAction(action);
    }

    @Getter
    @Setter
    public class DaDiPaoXiaoZSAddition {

        private LinkedList<BuffState> attBuffs = new LinkedList<>();

        private BuffState cureBuff;

    }
}
