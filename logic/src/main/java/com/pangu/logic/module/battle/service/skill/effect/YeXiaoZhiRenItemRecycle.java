package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.FilterType;
import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.ItemAdd;
import com.pangu.logic.module.battle.model.report.values.ItemRemove;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.select.utils.Rectangle;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import com.pangu.logic.module.battle.service.skill.param.StateAddParam;
import com.pangu.logic.module.battle.service.skill.param.YeXiaoZhiRenItemRecycleParam;
import com.pangu.logic.module.battle.service.skill.utils.SkillUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 当场上的羽毛达到10根时，艾莲娜下一次普通攻击召回所有羽刃，对穿透的敌军造成150%攻击力物理伤害。遭到3枚羽刃击中的敌人，将被眩晕0.5秒。
 */
@Component
public class YeXiaoZhiRenItemRecycle implements SkillEffect {
    @Autowired
    private HpHigherDamage higherDamage;

    @Override
    public EffectType getType() {
        return EffectType.YE_XIAO_ZHI_REN_ITEM_RECYCLE;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final YeXiaoZhiRenItemRecycleParam param = state.getParam(YeXiaoZhiRenItemRecycleParam.class);
        final HashMap<Unit, Integer> hitUnit2hitCount = new HashMap<>();
        final Point ownerPoint = owner.getPoint();
        final int width = param.getWidth();
        final List<Unit> enemies = FilterType.ENEMY.filter(owner, time);
        final Iterator<ItemAdd> iterator = owner.getItems().iterator();

        while (iterator.hasNext()) {
            final ItemAdd item = iterator.next();
            iterator.remove();
            skillReport.add(time, owner.getId(), new ItemRemove(item.getId()));

            //  计算弹道
            final Point itemPoint = item.getPoint();
            final Rectangle rectangle = new Rectangle(ownerPoint, itemPoint, width, ownerPoint.distance(itemPoint));

            //  统计命中单元
            for (Unit enemy : enemies) {
                if (rectangle.inRect(enemy.getPoint().x, enemy.getPoint().y)) {
                    hitUnit2hitCount.merge(enemy, 1, Integer::sum);
                }
            }
        }

        //  根据统计数据对命中单元造成伤害和控制
        final DamageParam dmgParam = param.getDmgParam();
        final int stateTriggerHitCount = param.getStateTriggerHitCount();
        final StateAddParam stateParam = param.getStateParam();
        state.setParamOverride(dmgParam);
        for (Map.Entry<Unit, Integer> entry : hitUnit2hitCount.entrySet()) {
            final Unit hitUnit = entry.getKey();
            final Integer hitCount = entry.getValue();

            for (int i = 0; i < hitCount; i++) {
                context.execAttackBeforePassiveAndEffect(time, higherDamage, hitUnit, skillState, state, skillReport);
            }
            if (hitCount >= stateTriggerHitCount) {
                SkillUtils.addState(owner, hitUnit, stateParam.getState(), time, stateParam.getTime() + time, skillReport, context);
            }
        }
        state.setParamOverride(null);
    }
}
