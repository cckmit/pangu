package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.*;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.ItemAdd;
import com.pangu.logic.module.battle.model.report.values.ItemMove;
import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.BattleConstant;
import com.pangu.logic.module.battle.service.action.EffectAction;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.select.utils.BestCircle;
import com.pangu.logic.module.battle.service.select.select.utils.Circle;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.AreaParam;
import com.pangu.logic.module.battle.service.skill.param.XingGuangNiLiuParam;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 星光逆流
 * 指挥占星球飞向敌方人多的区域,释放一股冲击波,对周围敌军造成135%的魔法伤害并在短暂延迟后将他们拉向占星球
 * 2级:伤害提升至180%
 * 3级:造成伤害后降低受伤敌人20%的攻击速度,持续4秒
 * 4级:每命中一个目标,则回复自己50点能量
 *
 * 此【主动】效果仅用于【移动道具，造成伤害】
 * 其余效果由相关【被动】执行
 * {@link com.pangu.logic.module.battle.service.passive.effect.XingGuangNiLiu}
 */
@Component
public class XingGuangNiLiu implements SkillEffect {
    @Autowired
    private HpMagicDamage hpMagicDamage;
    @Autowired
    private Move move;

    @Override
    public EffectType getType() {
        return EffectType.XING_GUANG_NI_LIU;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final XingGuangNiLiuParam param = state.getParam(XingGuangNiLiuParam.class);
        final List<Unit> enemies = FilterType.ENEMY.filter(owner, time);
        //计算出敌人最多的圆心
        final List<Point> enemyPoints = enemies.stream().map(Unit::getPoint).collect(Collectors.toList());
        final LinkedList<ItemAdd> items = owner.getItems();
        Point start;
        if (items == null || items.isEmpty()) {
            start = owner.getPoint();
        } else {
            start = items.get(0).getPoint();
        }
        final Point bestCenter = BestCircle.calBestPoint(start, enemyPoints, param.getR(), BattleConstant.MAX_X, BattleConstant.MAX_Y, false);
        //朝圆心位置投掷道具
        //若存在则移动，不存在则添加
        if (items == null || items.isEmpty()) {
            final int validTime = owner.getBattle().getConfig().getTime();
            final ItemAdd itemAdd = owner.addItem(time, ArrayUtils.toArray(bestCenter), validTime).get(0);
            final Point pointForReport = new Point(itemAdd.getPoint());
            final ItemAdd itemAddForReport = new ItemAdd(itemAdd.getId(), pointForReport, validTime);
            skillReport.add(time, owner.getId(), itemAddForReport);
        } else {
            final HashMap<ItemAdd, Point> itemAddPointHashMap = new HashMap<>();
            itemAddPointHashMap.put(items.get(0), bestCenter);
            final List<ItemMove> itemMoves = owner.moveItem(itemAddPointHashMap);
            for (ItemMove itemMove : itemMoves) {
                skillReport.add(time, owner.getId(), itemMove);
            }
        }

        //对该区域敌人造成伤害
        final Circle circle = new Circle(bestCenter.x, bestCenter.y, param.getR());
        state.setParamOverride(param.getDmg());
        final List<Unit> inCircle = new ArrayList<>();
        for (Unit enemy : enemies) {
            if (circle.inShape(enemy.getPoint().x, enemy.getPoint().y)) {
                inCircle.add(enemy);
                hpMagicDamage.execute(state, owner, enemy, skillReport, time, skillState, context);
            }
        }
        state.setParamOverride(null);
        //构造延迟选择策略
        final AreaParam areaParam = AreaParam.builder()
                .shape(AreaType.CIRCLE)
                .r(param.getR())
                .points(ArrayUtils.toArray(new int[]{bestCenter.x, bestCenter.y})).build();
        final SelectSetting selectSetting = SelectSetting.builder()
                .selectType(SelectType.AREA)
                .realParam(areaParam)
                .filter(FilterType.ENEMY).build();
        //提交一个延迟拉取效果
        final EffectState effectState = new EffectState(null, 0);
        effectState.setParamOverride(bestCenter);
        final EffectAction effectAction = new EffectAction(time + param.getDragDelay(), owner, skillState, skillReport, effectState, inCircle);
        effectAction.setDynamicSelectSetting(selectSetting);
        effectAction.setEffect(move);
        owner.getBattle().addWorldAction(effectAction);
    }
}
