package com.pangu.logic.module.battle.service.action;

import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Unit;

import java.util.List;

/**
 * 添加BUFF行为
 * @author Kubby
 */
public class AddBuffAction extends CloseableAction {

    private Unit caster;

    private Unit target;

    private List<String> buffIds;

    private int time;

    private boolean ignoreIfDead;

    private ITimedDamageReport damageReport;

    @Override
    public int getTime() {
        return time;
    }

    @Override
    public void execute() {
        if (ignoreIfDead && target.isDead()) {
            return;
        }
        for (String buffId : buffIds) {
            BuffFactory.addBuff(buffId, caster, target, time, damageReport, null);
        }
    }

    public static AddBuffAction of(Unit caster, Unit target, List<String> buffIds, boolean ignoreIfDead, int time,ITimedDamageReport damageReport) {
        AddBuffAction action = new AddBuffAction();
        action.caster = caster;
        action.target = target;
        action.buffIds = buffIds;
        action.ignoreIfDead = ignoreIfDead;
        action.time = time;
        action.damageReport = damageReport;
        return action;
    }
}
