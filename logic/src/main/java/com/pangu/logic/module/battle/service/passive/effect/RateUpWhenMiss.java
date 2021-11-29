package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.UnitRate;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.*;
import com.pangu.logic.module.battle.service.passive.param.RateUpWhenMissParam;
import org.springframework.stereotype.Component;

/**
 * 本轮攻击存在未命中目标时，下轮攻击提升命中率和伤害
 */
@Component
public class RateUpWhenMiss implements SkillReleasePassive, AttackBeforePassive {
    @Override
    public PassiveType getType() {
        return PassiveType.RATE_UP_WHEN_MISS;
    }

    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time, Context context, SkillReport skillReport) {
        final RateUpWhenMissParam param = passiveState.getParam(RateUpWhenMissParam.class);
        final Addition addition = passiveState.getAddition(Addition.class, new Addition());
        final Addition.HitUpState hitUpState = addition.hitUpState;
        switch (hitUpState) {
            //[必中]状态可使用时，提升命中率直至下一次释放技能
            case CONSUMABLE: {
                owner.increaseRate(UnitRate.HIT, param.getHitUpRate());
                owner.increaseRate(UnitRate.HARM_M, param.getHarmUpRate());
                owner.increaseRate(UnitRate.HARM_P, param.getHarmUpRate());
                addition.hitUpState = Addition.HitUpState.CONSUMING;
                break;
            }
            //[必中]状态处于使用时，说明上轮技能已享受命中率提升的加成
            case CONSUMING: {
                owner.increaseRate(UnitRate.HIT, -param.getHitUpRate());
                owner.increaseRate(UnitRate.HARM_M, -param.getHarmUpRate());
                owner.increaseRate(UnitRate.HARM_P, -param.getHarmUpRate());
                addition.hitUpState = Addition.HitUpState.CONSUMED;
                break;
            }
        }
    }

    @Override
    public void attackBefore(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time, Context context, SkillReport skillReport) {
    }

    // 若存在任意目标未被命中则将[必中]切换为可使用状态
    @Override
    public void attackEnd(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time, Context context, SkillReport skillReport) {
        if (!context.isMiss(target)) {
            return;
        }

        //内置cd
        final Addition addition = passiveState.getAddition(Addition.class, new Addition());
        if (addition.cdEndTime > time) {
            return;
        }

        final int cd = passiveState.getParam(RateUpWhenMissParam.class).getCd();
        addition.cdEndTime = time + cd;
    }

    private static class Addition {
        /**
         * cd结束时间
         */
        private int cdEndTime = -1;

        /**
         * 攻击强化状态
         */
        private HitUpState hitUpState = HitUpState.CONSUMED;

        public enum HitUpState {
            CONSUMABLE, CONSUMING, CONSUMED
        }
    }
}
