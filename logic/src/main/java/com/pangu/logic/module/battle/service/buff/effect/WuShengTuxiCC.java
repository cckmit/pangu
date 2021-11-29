package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.model.FilterType;
import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.model.report.values.PositionChange;
import com.pangu.logic.module.battle.service.BattleConstant;
import com.pangu.logic.module.battle.service.buff.Buff;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.buff.param.WuShengTuXiCCParam;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.WuShengTuXiParam;
import com.pangu.logic.module.battle.service.select.select.utils.TwoPointDistanceUtils;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 当有敌人出现在己方半区时，无视技能冷却时间，立刻使用无声突袭瞬间攻击靠近己方后排的敌人旁边，且使目标眩晕1.5秒
 */
@Component
public class WuShengTuxiCC implements Buff {
    @Override
    public BuffType getType() {
        return BuffType.WU_SHENG_TU_XI_CC;
    }

    @Override
    public void update(BuffState state, Unit unit, int time, Object addition) {
        final WuShengTuXiCCParam param = state.getParam(WuShengTuXiCCParam.class);
        final Integer cd = state.getAddition(Integer.class, -1);
        if (time < cd) {
            return;
        }

        //  既不是在在释放普攻时也不是在移动时，不执行此技能
        if (!unit.cancelAction(time)) {
            return;
        }

        final List<Unit> enemies = FilterType.ENEMY.filter(unit, time);
        final ArrayList<Unit> enemiesInSelfHalfArea = new ArrayList<>();
        final int centerX = BattleConstant.MAX_X / 2;
        if (unit.getFriend().isAttacker()) {
            for (Unit enemy : enemies) {
                if (enemy.getPoint().x - centerX < 0) {
                    enemiesInSelfHalfArea.add(enemy);
                }
            }
        } else {
            for (Unit enemy : enemies) {
                if (enemy.getPoint().x - centerX > 0) {
                    enemiesInSelfHalfArea.add(enemy);
                }
            }
        }

        //  己方战场不存在敌人，不触发
        if (enemiesInSelfHalfArea.size() <= 0) {
            return;
        }

        //  瞬移到敌方身后
        final Unit target = enemiesInSelfHalfArea.get(0);
        unit.setTarget(target);
        // 改变自身位置到敌人背后
        final Point ownerPoint = unit.getPoint();
        Point validPoint = TwoPointDistanceUtils.getNearEndPointDistance(ownerPoint, target.getPoint(), param.getDist());
        unit.move(validPoint);
        state.getBuffReport().add(time, unit.getId(), PositionChange.of(ownerPoint.x, ownerPoint.y));
        //  攻击
        final List<PassiveState> states = unit.getPassiveStateByType(PassiveType.WU_SHENG_TU_XI);
        if (states.isEmpty()) {
            return;
        }

        final PassiveState passiveState = states.get(0);
        final WuShengTuXiParam psvParam = passiveState.getParam(WuShengTuXiParam.class);
        SkillFactory.updateNextExecuteSkill(time, unit, param.replace(psvParam.getSkillId()));

        state.setAddition(time + param.getCd());
    }
}
