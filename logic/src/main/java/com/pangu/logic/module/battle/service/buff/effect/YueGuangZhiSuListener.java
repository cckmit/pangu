package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.model.report.BuffReport;
import com.pangu.logic.module.battle.resource.BuffSetting;
import com.pangu.logic.module.battle.service.buff.Buff;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.buff.param.YueGuangZhiSuListenerParam;
import com.pangu.logic.module.battle.service.core.Unit;
import org.springframework.stereotype.Component;

/**
 * 当此BUFF持有者连续5秒锁定同一个目标后，为自己和目标分别添加一种BUFF，切换目标后已添加的BUFF立即移除
 */
@Component
public class YueGuangZhiSuListener implements Buff {

    @Override
    public BuffType getType() {
        return BuffType.YUE_GUANG_ZHI_SU_LISTENER;
    }

    @Override
    public void update(BuffState state, Unit unit, int time, Object addition) {
        //  更新目标锁定数据
        final Unit curTarget = unit.getTarget();
        if (curTarget == null) {
            return;
        }
        final Addition traceInfo = state.getAddition(Addition.class, Addition.of(curTarget, time));
        final Unit preTarget = traceInfo.traceTarget;
        traceInfo.updateTarget(curTarget, time);

        final YueGuangZhiSuListenerParam param = state.getParam(YueGuangZhiSuListenerParam.class);
        final BuffSetting buffSetting = BuffFactory.getSetting(param.getBuff());
        final BuffSetting deBuffSetting = BuffFactory.getSetting(param.getDeBuff());
        //  移除上一个目标身上的DEBUFF以及因上一个目标而产生的BUFF
        final BuffState buffState = unit.getBuffStateByTag(buffSetting.getTag());
        final BuffState preTargetDeBuffState = preTarget.getBuffStateByTag(deBuffSetting.getTag());
        if (preTarget != curTarget) {
            if (preTargetDeBuffState != null && preTargetDeBuffState.getCaster() == unit) {
                BuffFactory.removeBuffState(preTargetDeBuffState, preTarget, time);
            }
            if (buffState != null) {
                BuffFactory.removeBuffState(buffState, unit, time);
            }
            return;
        }

        if (traceInfo.traceDur < param.getTriggerTraceDuration()) {
            return;
        }

        final BuffReport buffReport = state.getBuffReport();
        //已添加BUFF不重复添加，否则会覆盖之前累计的效果
        if (buffState == null) {
            BuffFactory.addBuff(buffSetting, unit, unit, time, buffReport, null);
        }
        if (curTarget.getBuffStateByTag(deBuffSetting.getTag()) == null) {
            BuffFactory.addBuff(deBuffSetting, unit, curTarget, time, buffReport, null);
        }
    }

    private static class Addition {
        /**
         * 当前锁定的目标
         */
        private Unit traceTarget;

        /**
         * 当前锁定目标的累计锁定时长
         */
        private int traceDur;

        /**
         * 上次更新时间
         */
        private int timeStamp;


        public void updateTarget(Unit traceTarget, int timeStamp) {
            if (this.traceTarget == traceTarget) {
                this.traceDur += timeStamp - this.timeStamp;
            } else {
                this.traceDur = 0;
                this.traceTarget = traceTarget;
            }
            this.timeStamp = timeStamp;
        }

        public static Addition of(Unit traceTarget, int timeStamp) {
            final Addition addition = new Addition();
            addition.traceTarget = traceTarget;
            addition.timeStamp = timeStamp;
            return addition;
        }
    }
}
