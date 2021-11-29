package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.HuoLiZhiYuanParam;
import com.pangu.logic.module.battle.service.skill.param.LuHaiBaZhuZSParam;
import org.springframework.stereotype.Component;

/**
 * 陆海霸主·巴达克专属装备
 * 1：火力支援命中的敌人，减少10%命中，持续到火力支援减速
 * 10：火力支援命中的敌人，减少10%攻速，持续到火力支援减速
 * 20：火力支援命中的敌人，增加敌人的被暴击率10%，持续到火力支援减速
 * 30：火力支援命中的敌人，被友军攻击时，友军的攻速+10%，持续到火力支援减速
 * @author Kubby
 */
@Component
public class LuHaiBaZhuZS implements SkillEffect {

    @Override
    public EffectType getType() {
        return EffectType.LU_HAI_BA_ZHU_ZS;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time,
                        SkillState skillState, Context context) {
        LuHaiBaZhuZSParam param = state.getParam(LuHaiBaZhuZSParam.class);
        for (SkillState ss : owner.getActiveSkills()) {
            for (EffectState es : ss.getEffectStates()) {
                if (es.getType() == EffectType.HUO_LI_ZHI_YUAN) {
                    HuoLiZhiYuanParam huoLiZhiYuanParam = es.getParam(HuoLiZhiYuanParam.class);
                    huoLiZhiYuanParam.setZsTargetBuffIds(param.getZsTargetBuffIds());
                    huoLiZhiYuanParam.setZsFriendBuffId(param.getZsFriendBuffId());
                }
            }
        }
    }
}
