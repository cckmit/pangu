package com.pangu.logic.module.battle.service.skill.effect;


import com.pangu.logic.module.battle.model.*;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.BattleConstant;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.param.AreaParam;
import com.pangu.logic.module.battle.service.skill.param.ShengMingShouGeParam;
import com.pangu.logic.module.battle.service.select.select.utils.BestCircle;
import com.pangu.logic.module.battle.service.select.select.utils.Circle;
import com.pangu.logic.module.battle.service.select.select.utils.Rectangle;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;


/**
 * 生命收割
 * 向敌人数量较多的一侧,合计投掷5把飞镰每把飞镰分别飞向一名敌人,造成220%攻击力的伤害,飞镰附带穿透效果,重复被命中的敌人,将只受到后续飞镰30%的伤害
 * 2级:伤害提升至240%攻击力
 * 3级:伤害提升至260%攻击力
 * 4级:伤害提升至300%攻击力
 */
@Component
public class ShengMingShouGe implements SkillEffect {
    @Autowired
    private HpPhysicsDamage hpPhysicsDamage;

    @Override
    public EffectType getType() {
        return EffectType.SHENG_MING_SHOU_GE;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final ShengMingShouGeParam param = state.getParam(ShengMingShouGeParam.class);
        final List<Unit> enemies = FilterType.ENEMY.filter(owner, time);
        final List<Point> enemyPoints = enemies.stream().map(Unit::getPoint).collect(Collectors.toList());
        //筛选出位于bestCircle中的目标
        final Point center = BestCircle.calBestPoint(owner.getPoint(), enemyPoints, param.getR(), BattleConstant.MAX_X, BattleConstant.MAX_Y, false);
        final Circle circle = new Circle(center.x, center.y, param.getR());
        final List<Unit> inCircle = enemies.stream().filter(enemy -> circle.inShape(enemy.getPoint().x, enemy.getPoint().y)).collect(Collectors.toList());

        //移除可命中目标中已命中的目标
        final Set<Unit> attackedUnits = getAddition(context);
        final List<Unit> attackedInCircle = new ArrayList<>();
        final List<Unit> nonAttackedInCircle = new ArrayList<>();
        for (Unit unit : inCircle) {
            if (attackedUnits.contains(unit)) {
                attackedInCircle.add(unit);
            } else {
                nonAttackedInCircle.add(unit);
            }
        }

        //从未命中过的目标中随机抽选一个目标，或从已命中目标中随机抽选一个
        Unit trueTarget = null;
        if (nonAttackedInCircle.size() > 0) {
            Collections.shuffle(nonAttackedInCircle);
            trueTarget = nonAttackedInCircle.get(0);
        } else if (attackedInCircle.size() > 0) {
            Collections.shuffle(attackedInCircle);
            trueTarget = attackedInCircle.get(0);
        }

        if (trueTarget == null) {
            return;
        }

        //以施法者和目标为锚点作矩形，筛选出所有位于矩形内部的目标
        final ArrayList<Unit> trueTargets = new ArrayList<>();
        final Rectangle rectangle = new Rectangle(owner.getPoint(), trueTarget.getPoint(), param.getWidth(), param.getLength());
        for (Unit unit : inCircle) {
            if (rectangle.inRect(unit.getPoint().x, unit.getPoint().y)) {
                trueTargets.add(unit);
            }
        }
        //执行伤害
        state.setParamOverride(param.getDmg());
        for (Unit unit : trueTargets) {
            hpPhysicsDamage.execute(state, owner, unit, skillReport, time, skillState, context);
        }
        state.setParamOverride(null);
        //将技能方向发给前端
        final AreaParam areaParam = AreaParam.builder()
                .shape(AreaType.POINT)
                .points(new int[][]{{trueTarget.getPoint().x, trueTarget.getPoint().y}})
                .build();
        skillReport.setAreaParam(areaParam, time);
        //记录已攻击单位的任务交由被动去完成
    }

    public Set<Unit> getAddition(Context context) {
        Set<Unit> addition = context.getRootSkillEffectAction().getAddition(Set.class);
        if (addition == null) {
            addition = new HashSet<>();
            context.getRootSkillEffectAction().setAddition(addition);
        }
        return addition;
    }
}
