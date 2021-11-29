package com.pangu.logic.module.battle.service.action.custom;

import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.service.action.CloseableAction;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.core.Unit;

/**
 * 飞行技师·比佛利
 * 1：只要自己在3秒内没有受到伤害,则自己造成的伤害提升20%
 * 10：只要自己在3秒内没有受到伤害,则自己造成的伤害提升30%
 * 20：只要自己在3秒内没有受到伤害,则自己造成的伤害提升40%
 * 30：只要自己在3秒内没有受到伤害,则自己造成的伤害提升50%
 *
 * @author Kubby
 */
public class FeiXingJiShiZSAction extends CloseableAction {

    /**
     * 拥有者
     */
    private Unit owner;
    /**
     * 加的BUFF所需间隔时间（单位：秒）
     */
    private int interval;
    /**
     * 加的BUFF标识
     */
    private String buffId;
    /**
     * 当前添加的BUFF状态
     */
    private BuffState buffState;
    /**
     * 计数器，每秒检测一次，用于判断是否达到加的BUFF所需间隔时间
     */
    private int counter;
    /**
     * 行为执行时间
     */
    private int time;

    private ITimedDamageReport damageReport;

    @Override
    public int getTime() {
        return time;
    }

    @Override
    public void execute() {
        if (owner.isDead() || buffState != null) {
            counter = 0;
        } else if (counter >= interval) {
            buffState = BuffFactory.addBuff(buffId, owner, owner, time, damageReport, null);
            counter = 0;
        } else {
            counter++;
        }
        gotoNext();
    }

    public void hurt(int time) {
        counter = 0;
    }

    private void gotoNext() {
        time += 1000;
        owner.getBattle().addWorldAction(this);
    }

    public static FeiXingJiShiZSAction of(Unit owner, String buffId, int interval, int time, ITimedDamageReport damageReport) {
        FeiXingJiShiZSAction action = new FeiXingJiShiZSAction();
        action.owner = owner;
        action.buffId = buffId;
        action.interval = interval;
        action.time = time;
        action.damageReport = damageReport;
        return action;
    }
}
