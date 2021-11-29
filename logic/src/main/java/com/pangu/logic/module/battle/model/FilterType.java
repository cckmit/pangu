package com.pangu.logic.module.battle.model;

import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.filter.*;
import com.pangu.framework.protocol.annotation.Transable;

import java.util.List;

/**
 * 技能目标过滤类型
 * author weihongwei
 * date 2018/3/26
 */
@Transable
public enum FilterType {

    /**
     * 自己
     */
    SELF(new SelfFilter()),

    /**
     * 自己（不管自己处于什么状态都会选中）
     */
    FORCE_SELF(new ForceSelfFilter()),

    /**
     * 其他的队伍
     */
    ENEMY(new EnemyFilter()),

    /**
     * 无视魅惑强行选中敌方
     */
    FORCE_ENEMY(new ForceEnemy()),

    /**
     * 自己的队伍
     */
    FRIEND(new FriendFilter()),

    /** 无视不可选中状态强行选中我方*/
    ALIVE_FRIEND(new AliveFriend()),

    /**
     * 除自己以外的友方
     */
    FRIEND_WITHOUT_SELF(new FriendWithoutSelfFilter()),

    /**
     * 最近攻击的目标
     */
    TARGET(new TargetFilter()),

    /**
     * 所有人
     */
    ALL(new AllFilter()),

    /**
     * 除自身以外的所有人
     */
    ALL_BUT_SELF(new AllButSelf()),

    /**
     * 优先友军前排，没有前排选其他
     */
    FRIEND_FRONT(new FriendFront()),

    /**
     * 敌军前排
     */
    ENEMY_FRONT(new EnemyFront()),

    /**
     * 只选择友方前排
     */
    FriendFrontOnly(new FriendFrontOnly()),

    /**
     * 只选择友方后排
     */
    FRIEND_BACK_ONLY(new FriendBackOnly()),

    /**
     * 只选择敌方后排
     */
    ENEMY_BACK_ONLY(new EnemyBackOnly()),

    /**
     * 没有目标
     */
    NO_UNIT(new NoUnit()),

    /**
     * 跟随者(类似牧师始终跟随战力最高的单元)
     */
    FOLLOWER(new Follower()),

    /**
     * 跟随者以及血量最低的单元
     */
    FOLLOWER_AND_MIN_HP(new FollowerAndMinHp());

    private final Filter filter;

    FilterType(Filter filter) {
        this.filter = filter;
    }

    public List<Unit> filter(Unit unit, int time) {
        return filter.list(unit, time);
    }
}