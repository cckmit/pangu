package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.ItemAdd;
import com.pangu.logic.module.battle.model.report.values.ItemRemove;
import com.pangu.logic.module.battle.model.report.values.Miss;
import com.pangu.logic.module.battle.model.report.values.PositionChange;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import com.pangu.logic.module.battle.service.skill.param.YeXiaoAnXiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * 夜枭暗袭
 * 瞬间移动到地上的一把羽毛处，对周围造成200%攻击力的，技能每12秒触发一次
 * 伤害。
 * 2级:伤害提升至240%攻击力
 * 3级:伤害提升至270%攻击力
 * 4级：每10秒触发一次该技能
 */
@Component
public class YeXiaoAnXi implements SkillEffect {

    @Autowired
    private HpPhysicsDamage hpPhysicsDamage;

    @Override
    public EffectType getType() {
        return EffectType.YE_XIAO_AN_XI;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        YeXiaoAnXiParam param = state.getParam(YeXiaoAnXiParam.class);
        int distance = param.getDistance();

        LinkedList<ItemAdd> knives = owner.getItems();
        if (knives == null) {
            return;
        }
        List<Unit> allLive = owner.getEnemy().getCurrent();
        Iterator<ItemAdd> iterator = knives.iterator();
        ItemAdd validKnife = null;
        OUTER:
        while (iterator.hasNext()) {
            ItemAdd knife = iterator.next();
            if (knife.getInvalidTime() < time) {
                iterator.remove();
                continue;
            }
            Point point = knife.getPoint();
            for (Unit unit : allLive) {
                if (!Unit.canBeSelect(unit, time)) {
                    continue;
                }
                if (unit.getPoint().distance(point) > distance) {
                    continue;
                }
                validKnife = knife;
                if (param.isItemCost()) {
                    iterator.remove();
                }
                break OUTER;
            }
        }
        if (validKnife == null) {
            skillReport.add(time, owner.getId(), new Miss());
            return;
        }
        Point point = validKnife.getPoint();
        state.setParamOverride(new DamageParam(param.getFactor()));
        for (Unit unit : allLive) {
            if (!Unit.canBeSelect(unit, time)) {
                continue;
            }
            if (unit.getPoint().distance(point) > distance) {
                continue;
            }
            hpPhysicsDamage.execute(state, owner, unit, skillReport, time, skillState, context);
        }
        point = new Point(point.x, point.y);
        state.setParamOverride(null);
        owner.move(point);
        final Point ownerPoint = owner.getPoint();
        skillReport.add(time, owner.getId(), PositionChange.of(ownerPoint.x, ownerPoint.y));
        if (param.isItemCost()) {
            skillReport.add(time, owner.getId(), new ItemRemove(validKnife.getId()));
        }
    }
}
