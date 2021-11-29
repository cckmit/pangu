package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Death;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.JuDuXuanWoParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 剧毒蛇影BOSS技能：剧毒漩涡
 * 召唤剧毒漩涡在一个敌人脚下引爆，被击中的目标减少300能量，并额外附带1层巫毒BUFF，该技能会直接击杀巫毒BUFF超过5层的敌人
 * @author Kubby
 */
@Component
public class JuDuXuanWo implements SkillEffect {

    @Override
    public EffectType getType() {
        return EffectType.JU_DU_XUAN_WO;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time,
                        SkillState skillState, Context context) {
        JuDuXuanWoParam param = state.getParam(JuDuXuanWoParam.class);
        List<BuffState> buffStates = target.getBuffBySettingId(param.getBuffId());
        int count = buffStates.size();
        if (count >= param.getDeathOverlayCount()) {
            target.foreverDead();
            skillReport.add(time, target.getId(), new Death());
            for (BuffState buffState : buffStates) {
                BuffFactory.removeBuffState(buffState, target, time);
            }
        }
    }
}
