package com.pangu.logic.module.battle.service.action.custom;

import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.StateAdd;
import com.pangu.logic.module.battle.model.report.values.StateRemove;
import com.pangu.logic.module.battle.service.Battle;
import com.pangu.logic.module.battle.service.action.Action;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.skill.effect.DaDiPaoXiaoZS;
import com.pangu.logic.module.battle.service.skill.param.DaDiPaoXiaoZSParam;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 大地咆哮·塔巴斯专属装备
 * 1：战斗中，身边每有一个友军，自身攻击力+3%
 * 10：战斗中，身边每有一个友军，自身攻击力+5%
 * 20：战斗中，身边每有一个友军，自身攻击力+8%
 * 30：当场上敌友军总量大于等于5的时候，自身每秒回复5%生命值，并免控
 * @author Kubby
 */
public class DaDiPaoXiaoZSAction implements Action {

    private int time;

    private Unit owner;

    private EffectState effectState;

    private SkillReport skillReport;

    @Override
    public int getTime() {
        return time;
    }

    @Override
    public void execute() {
        // 死亡后不再执行计算
        if (owner.isDead()) {
            return;
        }

        DaDiPaoXiaoZSParam param = effectState.getParam(DaDiPaoXiaoZSParam.class);
        DaDiPaoXiaoZS.DaDiPaoXiaoZSAddition addition = effectState
                .getAddition(DaDiPaoXiaoZS.DaDiPaoXiaoZSAddition.class);


        fixAddition(addition, owner);

        List<Unit> units = TargetSelector.select(owner, param.getSelectId(), time);

        LinkedList<BuffState> attBuffs = addition.getAttBuffs();
        int diff = attBuffs.size() - units.size();



        if (diff > 0) {
            for (int i = 0; i < diff; i++) {
                BuffState buffState = BuffFactory.addBuff(param.getAttBuffId(), owner, owner, time, skillReport, null);
                attBuffs.add(buffState);
            }
        } else {
            for (int i = 0; i < -diff; i++) {
                BuffState buffState = attBuffs.poll();
                if (buffState == null) {
                    break;
                }
                BuffFactory.removeBuffState(buffState, owner, time);
            }
        }



        if (!StringUtils.isBlank(param.getCureBuffId())) {
            int total = owner.getFriend().getCurrent().size() + owner.getEnemy().getCurrent().size();

            if (total >= param.getCureRequire()) {
                if (addition.getCureBuff() == null) {
                    BuffState buffState = BuffFactory.addBuff(param.getCureBuffId(), owner, owner, time, skillReport, null);
                    addition.setCureBuff(buffState);
                }
                if (!owner.hasState(UnitState.BA_TI, time)) {
                    owner.addState(UnitState.BA_TI);
                    skillReport.add(time, owner.getId(), new StateAdd(UnitState.BA_TI, Integer.MAX_VALUE));
                }
            } else {
                if (addition.getCureBuff() != null) {
                    BuffFactory.removeBuffState(addition.getCureBuff(), owner, time);
                    addition.setCureBuff(null);
                }
                if (owner.hasState(UnitState.BA_TI, time)) {
                    owner.removeState(UnitState.BA_TI);
                    skillReport.add(time, owner.getId(), new StateRemove(Collections.singletonList(UnitState.BA_TI)));
                }
            }
        }


        gotoNextAction();
    }

    private void fixAddition(DaDiPaoXiaoZS.DaDiPaoXiaoZSAddition addition, Unit owner) {
        LinkedList<BuffState> attBuffs = addition.getAttBuffs();
        attBuffs.removeIf(buffState -> owner.getBuffStateByTag(buffState.getTag()) == null);
        if (addition.getCureBuff() != null) {
            if (owner.getBuffStateByTag(addition.getCureBuff().getTag()) == null) {
                addition.setCureBuff(null);
            }
        }
    }

    private void gotoNextAction() {
        DaDiPaoXiaoZSParam param = effectState.getParam(DaDiPaoXiaoZSParam.class);
        time += Math.max(500, param.getInterval());
        owner.addTimedAction(this);
    }

    public static DaDiPaoXiaoZSAction of(int time, Unit owner, EffectState effectState, SkillReport skillReport) {
        DaDiPaoXiaoZSAction action = new DaDiPaoXiaoZSAction();
        action.time = time;
        action.owner = owner;
        action.effectState = effectState;
        action.skillReport = skillReport;
        return action;
    }
}
