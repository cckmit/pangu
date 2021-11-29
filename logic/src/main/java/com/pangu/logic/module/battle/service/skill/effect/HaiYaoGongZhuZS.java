package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.HaiYaoGongZhuZSParam;
import org.springframework.stereotype.Component;

/**
 * 海妖公主·卡莉安娜专属装备
 * 1：开局拥有2层自然庇护效果，当受到超过10%生命最大值伤害的时候，使用一层抵消该伤害，使用大招之后回复至满层
 * 10：分身拥有一半的层数
 * 20：4层自然庇护效果
 * 30：大招释放后，恢复3层
 * @author Kubby
 */
@Component
public class HaiYaoGongZhuZS implements SkillEffect {

    @Override
    public EffectType getType() {
        return EffectType.HAI_YAO_GONG_ZHU_ZS;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time,
                        SkillState skillState, Context context) {
        HaiYaoGongZhuZSParam param = state.getParam(HaiYaoGongZhuZSParam.class);
        BuffState buffState = BuffFactory.addBuff(param.getBuffId(), owner, owner, time, skillReport, null);
        buffState.setAddition(param.getOverlayTimes());
    }
}
