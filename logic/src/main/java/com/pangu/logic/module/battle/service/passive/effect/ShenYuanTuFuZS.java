package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.UnitRate;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.resource.FightSkillSetting;
import com.pangu.logic.module.battle.service.action.AddBuffAction;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackBeforePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillReleasePassive;
import com.pangu.logic.module.battle.service.passive.param.ShenYuanTuFuZSParam;
import com.pangu.framework.resource.Storage;
import com.pangu.framework.resource.anno.Static;
import org.springframework.stereotype.Component;

/**
 * 深渊屠夫·席恩专属装备
 * 1：普攻攻击时，若当场上存在生命值低于40%的目标时，使用无情钩链将目标拉至自己面前
 * 10：无情钩链使用后，4秒内攻速+15%
 * 20：无情钩链使用后，4秒内攻速+30%
 * 30：自身生命低于50%时候，大招伤害翻倍，20秒触发
 *
 * @author Kubby
 */
@Component
public class ShenYuanTuFuZS implements SkillReleasePassive, AttackBeforePassive {

    @Static
    private Storage<String, FightSkillSetting> skillStorage;

    @Override
    public PassiveType getType() {
        return PassiveType.SHEN_YUAN_TU_FU_ZS;
    }

    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time,
                             Context context, SkillReport skillReport) {
        ShenYuanTuFuZSParam param = passiveState.getParam(ShenYuanTuFuZSParam.class);
        if (param.getSkillTags().contains(skillState.getTag())) {
            owner.addTimedAction(AddBuffAction.of(owner, owner, param.getBuffIds(), true, time, skillReport));
        }
    }

    @Override
    public void attackBefore(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time,
                             Context context, SkillReport skillReport) {
        FightSkillSetting skillSetting = skillStorage.get(skillReport.getSkillId(), true);
        if (skillSetting.getType() != SkillType.SPACE) {
            return;
        }
        ShenYuanTuFuZSParam param = passiveState.getParam(ShenYuanTuFuZSParam.class);
        if (param.getHarmUpRate() <= 0) {
            return;
        }
        if (owner.getHpPct() > param.getHarmUpHpPct()) {
            return;
        }

        ShenYuanTuFuZSAddition addition = passiveState
                .getAddition(ShenYuanTuFuZSAddition.class, new ShenYuanTuFuZSAddition());

        /* CD中 */
        if (addition.lastActiveTime + param.getHarmUpCd() >= time) {
            return;
        }

        owner.increaseRate(UnitRate.HARM_P, param.getHarmUpRate());
        owner.increaseRate(UnitRate.HARM_M, param.getHarmUpRate());

        addition.harmUp = true;
        addition.lastActiveTime = time;
    }

    @Override
    public void attackEnd(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time,
                          Context context, SkillReport skillReport) {
        FightSkillSetting skillSetting = skillStorage.get(skillReport.getSkillId(), true);
        if (skillSetting.getType() != SkillType.SPACE) {
            return;
        }
        ShenYuanTuFuZSParam param = passiveState.getParam(ShenYuanTuFuZSParam.class);
        if (param.getHarmUpRate() <= 0) {
            return;
        }

        ShenYuanTuFuZSAddition addition = passiveState
                .getAddition(ShenYuanTuFuZSAddition.class, new ShenYuanTuFuZSAddition());

        if (addition.harmUp) {
            owner.increaseRate(UnitRate.HARM_P, -param.getHarmUpRate());
            owner.increaseRate(UnitRate.HARM_M, -param.getHarmUpRate());
        }

        addition.harmUp = false;
    }

    public static class ShenYuanTuFuZSAddition {

        private boolean harmUp;

        private int lastActiveTime;

    }

}
