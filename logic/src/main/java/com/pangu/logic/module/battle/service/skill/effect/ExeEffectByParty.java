package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.action.EffectAction;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import com.pangu.logic.module.battle.service.skill.param.ExeEffectByPartyParam;
import com.pangu.logic.module.battle.service.skill.param.ExeEffectByPartyParam.Party;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 根据不同阵营选择执行不同效果，ZHI_YU_HE_XIAN的抽象升级版。可自由配置需要执行的效果
 */
@Component
public class ExeEffectByParty implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.EXE_EFFECT_BY_PARTY;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final ExeEffectByPartyParam param = state.getParam(ExeEffectByPartyParam.class);
        final Map<Party, List<String>> partyAndEffect = param.getPartyAndEffect();
        final Set<Map.Entry<Party, List<String>>> entries = partyAndEffect.entrySet();
        for (Map.Entry<Party, List<String>> entry : entries) {
            if (entry.getKey() == Party.ENEMY && owner.getFriend() == target.getFriend()) continue;
            if (entry.getKey() == Party.FRIEND && owner.getFriend() != target.getFriend()) continue;
            createEffectActions(owner, target, skillReport, time, skillState, entry.getValue());
        }
    }

    private void createEffectActions(Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, List<String> effectIds) {
        for (String effectId : effectIds) {
            final EffectState effectState = SkillFactory.getEffectState(effectId);
            final EffectAction effectAction = new EffectAction(time, owner, skillState, skillReport, effectState, new ArrayList<Unit>() {{
                add(target);
            }});
            owner.getBattle().addWorldAction(effectAction);
        }
    }
}
