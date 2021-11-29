package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.FilterType;
import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.ItemAdd;
import com.pangu.logic.module.battle.model.report.values.ItemMove;
import com.pangu.logic.module.battle.service.BattleConstant;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.select.utils.BestRectangle;
import com.pangu.logic.module.battle.service.select.select.utils.Rectangle;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.DaDiSiLieParam;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 星爆:
 * 开战后,每过7秒角色会指挥占星球,朝目标区域发射,对沿途目标造成130%魔法伤害(对后续目标伤害递减)指令结束后占星球会停在目标区域 <br>
 * 2级:伤害提升至145% <br>
 * 3级:触发时间降低为5秒 <br>
 * 4级:伤害提升至160% <br>
 * <p>
 * 此【主动】效果负责【计算最佳矩形，并造成伤害且移动星球】
 * 其他效果由以下【被动】实现：
 * {@link com.pangu.logic.module.battle.service.passive.effect.XingBao}
 */
@Component
@Deprecated
public class XingBao implements SkillEffect {
    private final int MAX_X = BattleConstant.MAX_X;
    private final int MAX_Y = BattleConstant.MAX_Y;
    @Autowired
    private HpMagicDamage hpMagicDamage;

    @Override
    public EffectType getType() {
        return EffectType.XING_BAO;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final DaDiSiLieParam param = state.getParam(DaDiSiLieParam.class);
        //获取战场上所有敌人
        final List<Unit> enemies = FilterType.ENEMY.filter(owner, time);
        final Point[] points = enemies.stream().map(Unit::getPoint).toArray(Point[]::new);
        //计算最佳矩形
        final LinkedList<ItemAdd> items = owner.getItems();
        Point start;
        if (items == null || items.isEmpty()) {
            start = owner.getPoint();
        } else {
            start = items.get(0).getPoint();
        }
        final BestRectangle.PointInfo info = BestRectangle.calBestRectangle(start, points, param.getWidth(), param.getLength());
        if (info == null) {
            return;
        }
        //指挥飞行道具到达指定位置
        final Rectangle bestRect = info.getRectangle();
        final Point destination = bestRect.calMidPointOnOppositeSide();
        //若目的地位于场地之外，调整坐标位置
        final Point trueDestination = locationModifier(start, destination);

        if (items == null || items.isEmpty()) {
            final int validTime = owner.getBattle().getConfig().getTime();
            final ItemAdd itemAdd = owner.addItem(time, ArrayUtils.toArray(trueDestination), validTime).get(0);
            final Point pointForReport = new Point(itemAdd.getPoint());
            final ItemAdd itemAddForReport = new ItemAdd(itemAdd.getId(), pointForReport, validTime);
            skillReport.add(time, owner.getId(), itemAddForReport);
        } else {
            final HashMap<ItemAdd, Point> itemAddPointHashMap = new HashMap<>();
            itemAddPointHashMap.put(items.get(0), trueDestination);
            final List<ItemMove> itemMoves = owner.moveItem(itemAddPointHashMap);
            for (ItemMove itemMove : itemMoves) {
                skillReport.add(time, owner.getId(), itemMove);
            }
        }
        //对路径上的所有目标计算基础伤害
        final ArrayList<Unit> inRect = new ArrayList<>();
        state.setParamOverride(param.getDmg());
        for (Unit enemy : enemies) {
            if (bestRect.inRect(enemy.getPoint().x, enemy.getPoint().y)) {
                hpMagicDamage.execute(state, owner, enemy, skillReport, time, skillState, context);
                inRect.add(enemy);
            }
        }
        state.setParamOverride(null);
        //将受到伤害的目标按距离远近缓存，供被动减免伤害
        inRect.sort(Comparator.comparingInt(o -> o.getPoint().distance(owner.getPoint())));
        context.getRootSkillEffectAction().setAddition(inRect);
    }

    private Point locationModifier(Point start, Point target) {
        //获取待调整目标点至各边界的距离
        final int toMaxX = target.x - MAX_X;
        final int toMaxY = target.y - MAX_Y;
        final int toMinX = target.x;
        final int toMinY = target.y;

        //位于边界内部时不做任何修正
        if (toMinX * toMaxX <= 0 && toMinY * toMaxY <= 0) {
            return target;
        }

        //计算起点、终点作为锚点所确定的方程
        //垂直移动特殊处理
        if (target.x - start.x == 0) {
            return new Point(target.x, target.y > MAX_Y ? MAX_Y : 0);
        }
        //一般情况适用方程
        double k = (target.y - start.y) / 1.0 / (target.x - start.x);
        double b = (-k * start.x + start.y);
        //k*x-k*start.x+start.y=y
        //k*x+b=y, x=(y-b)/k
        //求该方程和四条边界的交点，交点如果位于指定边界内，则该交点有效，作为调整后的目标点
        final Point zeroXCrossPoint = new Point(0, (int) b);
        if (zeroXCrossPoint.y <= MAX_Y && zeroXCrossPoint.y >= 0 && target.x < 0) {
            return zeroXCrossPoint;
        }
        final Point maxXCrossPoint = new Point(MAX_X, (int) (k * MAX_X + b));
        if (maxXCrossPoint.y <= MAX_Y && zeroXCrossPoint.y >= 0 && target.x > MAX_X) {
            return maxXCrossPoint;
        }
        final Point zeroYCrossPoint = new Point((int) (-b / k), 0);
        if (zeroYCrossPoint.x <= MAX_X && zeroYCrossPoint.x >= 0 && target.y < 0) {
            return zeroYCrossPoint;
        }
        final Point maxYCrossPoint = new Point((int) ((MAX_Y - b) / k), MAX_Y);
        if (maxYCrossPoint.x <= MAX_X && maxYCrossPoint.x >= 0 && target.y > MAX_Y) {
            return maxYCrossPoint;
        }
        return target;
    }
}
