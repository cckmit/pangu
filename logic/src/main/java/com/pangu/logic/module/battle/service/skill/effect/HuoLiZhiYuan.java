package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.FilterType;
import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.BattleConstant;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.select.utils.BestCircle;
import com.pangu.logic.module.battle.service.select.select.utils.Circle;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import com.pangu.logic.module.battle.service.skill.param.HuoLiZhiYuanParam;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 陆海霸主·巴达克技能：火力支援
 * 1级：从空中落下一片炮弹雨,炮弹雨覆盖区域中的敌方武将护甲降低20%,攻击命中率降低30%,且每秒造成90%物理攻击伤害,持续4秒
 * 2级：护甲削弱提升至35%
 * 3级：在毒雾中自己的暴击率提升20%
 * 4级：持续时间提升至6秒
 *
 * @author Kubby
 */
@Component
public class HuoLiZhiYuan implements SkillEffect {

    @Autowired
    private HpPhysicsDamage physicsDamage;

    @Override
    public EffectType getType() {
        return EffectType.HUO_LI_ZHI_YUAN;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time,
                        SkillState skillState, Context context) {
        HuoLiZhiYuanParam param = state.getParam(HuoLiZhiYuanParam.class);

        HuoLiZhiYuanAddition addition = state.getAddition(HuoLiZhiYuanAddition.class, new HuoLiZhiYuanAddition());

        
        final List<Unit> enemies = FilterType.ENEMY.filter(owner, time);
        if (addition.point == null) {
            List<Point> enemyPoints = enemies.stream().map(Unit::getPoint)
                    .collect(Collectors.toList());
            addition.point = BestCircle
                    .calBestPoint(owner.getPoint(), enemyPoints, param.getRadius(), BattleConstant.MAX_X,
                            BattleConstant.MAX_Y, false);
        }

        if (addition.point == null) {
            return;
        }

        Circle circle = new Circle(addition.point.getX(), addition.point.getY(), param.getRadius());


        
        if (!StringUtils.isBlank(param.getOwnerBuffId())) {
            if (circle.inShape(owner.getPoint().getX(), owner.getPoint().getY())) {
                BuffFactory.addBuff(param.getOwnerBuffId(), owner, owner, time, skillReport, null);
            }
        }

        EffectState effectState = new EffectState(null, 0);
        effectState.setParamOverride(new DamageParam(param.getFactor()));

        
        Set<Unit> targets = new HashSet<>();
        for (Unit enemy : enemies) {
            if (circle.inShape(enemy.getPoint().getX(), enemy.getPoint().getY())) {
                for (String targetBuffId : param.getTargetBuffIds()) {
                    BuffFactory.addBuff(targetBuffId, owner, enemy, time, skillReport, null);
                }
                physicsDamage.execute(effectState, owner, enemy, skillReport, time, skillState, context);
                
                for (String targetBuffId : param.getZsTargetBuffIds()) {
                    BuffFactory.addBuff(targetBuffId, owner, enemy, time, skillReport, null);
                }
                targets.add(enemy);
            }
        }

        
        if (!StringUtils.isBlank(param.getZsFriendBuffId())) {
            for (Unit friend : FilterType.FRIEND.filter(owner, time)) {
                Unit currTarget = friend.getTarget();
                if (currTarget != null && targets.contains(currTarget)) {
                    BuffFactory.addBuff(param.getZsFriendBuffId(), owner, friend, time, skillReport, null);
                }
            }
        }

        
        if (context.getLoopTimes() >= skillState.getExecuteTimes()) {
            state.setAddition(null);
        }
    }

    @Getter
    public static class HuoLiZhiYuanAddition {

        private Point point;

    }
}
