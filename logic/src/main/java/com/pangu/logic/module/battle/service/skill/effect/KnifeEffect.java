package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.ItemAdd;
import com.pangu.logic.module.battle.service.BattleConstant;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用于生成飞刀
 */
@Component
public class KnifeEffect implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.KNIFE;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        Point point = target.getPoint();
        int change = BattleConstant.SCOPE;
        if (owner.getPoint().x > target.getPoint().x) {
            change = -change;
        }
        Point[] points = {new Point(point.x + change, point.y)};
        List<ItemAdd> items = owner.addItem(time, points);
        for (ItemAdd item : items) {
            skillReport.add(time, owner.getId(), item);
        }
    }
}
