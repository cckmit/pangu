package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Immune;
import com.pangu.logic.module.battle.model.report.values.PositionChange;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.select.utils.TwoPointDistanceUtils;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.WuQingGouLianParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 无情钩链：
 * 在普通攻击范围内没有敌人时，将一名敌人钩到自己面前，优先选取处于对称位置的敌人。
 * 2级:命中后使目标流血，每秒受到60%攻击力的伤害，持续8秒。
 * 3级:命中后使目标流血，每秒受到65%攻击力的伤害，持续8秒。
 */
@Component
public class WuQingGouLian implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.WU_QING_GOU_LIAN;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        Point targetPoint = target.getPoint();
        Point ownerPoint = owner.getPoint();
//        int distance = ownerPoint.distance(targetPoint);
//        if (distance <= owner.getMinMoveDistance()) {
//            return;
//        }
        WuQingGouLianParam param = state.getParam(WuQingGouLianParam.class);
        int range = param.getRange();
        Point nearStartPoint = TwoPointDistanceUtils.getNearStartPoint(ownerPoint, range, targetPoint);

        final String targetId = target.getId();
        if (target.immuneControl(time)) {
            skillReport.add(time, targetId, new Immune());
        } else {
            target.move(nearStartPoint);
        }
        skillReport.add(time, targetId, PositionChange.of(targetPoint.x, targetPoint.y));

        String buffId = param.getBuf();
        if (StringUtils.isNotEmpty(buffId)) {
            BuffFactory.addBuff(buffId, owner, target, time, skillReport, null);
        }
    }
}
