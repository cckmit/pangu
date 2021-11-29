package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.YongHengShouWangZSParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 永恒守望·奥米茄专属装备
 * 1：当自己布阵在前排时候，后排的队友，双防+10%
 * 10：当自己布阵在前排时候，后排的队友，双防+15%
 * 20：当自己布阵在前排时候，后排的队友，双防+20%
 * 30：当自己布阵在前排时候，后排的队友，双防+25%
 * @author Kubby
 */
@Component
public class YongHengShouWangZS implements SkillEffect {

    @Override
    public EffectType getType() {
        return EffectType.YONG_HENG_SHOU_WANG_ZS;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time,
                        SkillState skillState, Context context) {
        YongHengShouWangZSParam param = state.getParam(YongHengShouWangZSParam.class);

        /* 自己在前排 */
        if (!param.getSequences().contains(owner.getSequence())) {
            return;
        }

        /* 找出后排英雄 */
        List<Unit> units = TargetSelector.select(owner, param.getSelectId(), time);

        /* 添加属性BUFF */
        for (Unit unit : units) {
            BuffFactory.addBuff(param.getBuffId(), owner, unit, time, skillReport, null);
        }
    }
}
