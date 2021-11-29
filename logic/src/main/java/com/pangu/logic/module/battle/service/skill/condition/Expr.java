package com.pangu.logic.module.battle.service.skill.condition;

import com.pangu.logic.module.battle.model.FilterType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.utils.ExpressionHelper;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 表达式
 *
 * @author Kubby
 */
public class Expr implements SkillReleaseCondition<String> {

    @Override
    public boolean valid(SkillState skillState, Unit unit, int time, String param) {
        ExprContext context = ExprContext.of(skillState, unit, time);
        return ExpressionHelper.invoke(param, boolean.class, context);
    }

    @Getter
    public static class ExprContext {

        private SkillState skillState;

        private Unit owner;

        private int time;

        public boolean ownerHasBuffById(String buffId) {
            return !owner.getBuffBySettingId(buffId).isEmpty();
        }

        public boolean enemySelectable() {
            return !CollectionUtils.isEmpty(FilterType.ENEMY.filter(owner, time));
        }

        public static ExprContext of(SkillState skillState, Unit owner, int time) {
            ExprContext exprContext = new ExprContext();
            exprContext.skillState = skillState;
            exprContext.owner = owner;
            exprContext.time = time;
            return exprContext;
        }

        public boolean anyEnemyHpHigherThanOwner() {
            final List<Unit> enemies = FilterType.ENEMY.filter(owner, time);
            for (Unit enemy : enemies) {
                if (enemy.getValue(UnitValue.HP) > owner.getValue(UnitValue.HP)) {
                    return true;
                }
            }
            return false;
        }

        public boolean hasState(String state) {
            return owner.hasState(state, time);
        }
    }
}
