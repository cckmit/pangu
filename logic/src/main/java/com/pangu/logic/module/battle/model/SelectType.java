package com.pangu.logic.module.battle.model;

import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.HalfCourtSelect;
import com.pangu.logic.module.battle.service.select.select.*;
import com.pangu.framework.protocol.annotation.Transable;

import java.util.List;

/**
 * 选择类型
 * author weihongwei
 * date 2017/11/14
 */
@Transable
public enum SelectType {
    /**
     * 选择英雄单元
     */
    HERO(new HeroSelect()),

    /**
     * 选择参照系的召唤物
     */
    SUMMONED(new SummonedSelect()),

    /**
     * 选择位于以自身周围以外的目标
     */
    WITHOUT_SELF_CIRCLE(new WithoutSelfCircle()),
    /**
     * 选择普攻射程高于指定数值的单元
     */
    NORMAL_ATK_DIST_MORE_THAN(new NormalAtkDistanceMoreThan()),
    NORMAL_ATK_DIST_LESS_THAN(new NormalAtkDistanceLessThan()),

    /**
     * 选择我方或敌方半场
     */
    HALF_COURT(new HalfCourtSelect()),

    /**
     * 根据传入坐标进行选择
     */
    COORDINATE_FAN_SHAPED(new CoordinateFanShapedSelect()),

    /**
     * 从给定区域筛选目标
     */
    AREA(new AreaSelect()),

    /**
     * 筛选出最密集区域的敌人
     */
    BEST_CIRCLE(new BestCircleSelect()),

    /**
     * 以施法者为起点，任意目标为终点，筛选出以二者连线固定的包含最多目标的矩形
     */
    BEST_RECTANGLE(new BestRectangleSelect()),

    /**
     * 以随机目标为圆心，筛选给定半径内的目标
     */
    RANDOM_TARGET_CIRCLE(new RandomTargetCircle()),

    /**
     * 目标地为圆心 圆形目标
     */
    TARGET_CIRCLE(new TargetCircle()),

    /**
     * 以目标为圆心,排除目标自身
     */
    TARGET_CIRCLE_NO_TARGET(new TargetCircleNoTarget()),

    /**
     * 自身为圆心 圆形目标
     */
    SELF_CIRCLE(new SelfCircle()),

    /**
     * 平行于横轴的矩形，参照系位于矩形中心
     */
    HORIZONTAL_ENCLOSING_RECTANGLE(new HorizontalEnclosingRectangleSelect()),

    /**
     * 矩形目标
     */
    RECTANGLE(new RectangleSelect()),

    /**
     * 中轴平行于横轴的两瓣扇形，关于参照系所在
     */
    HORIZONTAL_ENCLOSING_FAN(new HorizontalEnclosingFanSelect()),

    /**
     * 扇形目标选择
     */
    FAN_SHAPED(new FanShapeSelect()),

    /**
     * 周围地方目标
     */
    AROUND_ENEMY(new AroundEnemy()),

    /**
     * 英雄类型
     */
    HERO_JOB(new HeroJobSelector()),

    /**
     * 除了某些职业的英雄
     */
    WITHOUT_HERO_JOB(new WithoutHeroJob()),

    /**
     * 随机选择
     */
    RANDOM(new RandomSelect()),

    /**
     * 最远距选择器
     */
    FARTHEST_DISTANCE(new FarthestDistanceSelect()),

    /**
     * 位置选择器（站位位置）
     */
    SEQUENCE(new SequenceSelect()),

    /**
     * 英雄类型选择器
     */
    UNIT_TYPE(new UnitTypeSelect()),

    /**
     * 英雄种族选择器
     */
    HERO_RACE(new HeroRaceSelect()),

    /**
     * 跟随者为圆心判断
     */
    FOLLOWER_CIRCLE(new FollwerEnemy()),

    /**
     * 选择一个周围地方人追多的队伍
     */
    MOST_ENEMY(new MostEnemySelect()),

    /**
     * 直线距离后的圆形范围
     */
    DISTANCE_CIRCLE(new DistanceCircle()),

    /**
     * 相邻站位选择器
     */
    ADJOIN_SEQUENCE(new AdjoinSequenceSelect()),

    /**
     * 根据血量百分比
     */
    HP_PCT(new HpPct()),

    /**
     * 根据血量百分比，优先选择非霸体单元
     */
    HP_PCT_AVOID_BA_TI(new HpPctAvoidBaTiSelect()),

    /**
     * 以场景中线为轴，对面位置
     */
    OPPOSITE_POSITION(new OppositePositionSelect()),

    /**
     * 以场景中线为轴，对面位置 <br>
     * 避免选取霸体单位，除非不存在非霸体单位
     */
    OPPOSITE_POSITION_AVOID_BA_TI(new OppositePositionAvoidBaTiSelect()),



    UNIT_ARROUND(new UnitAround()),

    ;
    // 选择器
    private final Selector selector;

    SelectType(Selector selector) {
        this.selector = selector;
    }

    public Selector getSelector() {
        return selector;
    }

    public List<Unit> select(Unit owner, List<Unit> units, SelectSetting selectSetting, int time) {
        return this.selector.select(owner, units, selectSetting, time);
    }
}
