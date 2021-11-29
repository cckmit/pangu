package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Immune;
import com.pangu.logic.module.battle.resource.BuffSetting;
import com.pangu.logic.module.battle.resource.FightSkillSetting;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.*;
import com.pangu.logic.module.battle.service.passive.param.HaiYaoGongZhuZSPassiveParam;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import com.pangu.framework.resource.Storage;
import com.pangu.framework.resource.anno.Static;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 海妖公主·卡莉安娜专属装备
 * 1：开局拥有2层自然庇护效果，当受到超过10%生命最大值伤害的时候，使用一层抵消该伤害，使用大招之后回复至满层
 * 10：分身拥有一半的层数
 * 20：4层自然庇护效果
 * 30：大招释放后，恢复3层
 *
 * @author Kubby
 */
@Slf4j
@Component
public class HaiYaoGongZhuZSPassive implements SkillReleasePassive, AttackBeforePassive, DamagePassive {

    @Static
    private Storage<String, BuffSetting> buffStorage;
    @Static
    private Storage<String, FightSkillSetting> skillStorage;

    @Override
    public PassiveType getType() {
        return PassiveType.HAI_YAO_GONG_ZHU_ZS;
    }

    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time,
                             Context context, SkillReport skillReport) {
        if (skillState.getType() != SkillType.SPACE) {
            return;
        }
        int nextTriggerTime = passiveState.getAddition(int.class, -1);
        if (time <= nextTriggerTime) {
            return;
        }

        HaiYaoGongZhuZSPassiveParam param = passiveState.getParam(HaiYaoGongZhuZSPassiveParam.class);
        BuffSetting buffSetting = buffStorage.get(param.getBuffId(), true);
        BuffState buffState = owner.getBuffStateByTag(buffSetting.getTag());
        if (buffState == null) {
            return;
        }
        buffState.setAddition(param.getRecoverOverlayTimes());
        passiveState.setAddition(time + param.getRecoverCd());
    }

    @Override
    public void attackBefore(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time,
                             Context context, SkillReport skillReport) {
    }

    @Override
    public void attackEnd(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time,
                          Context context, SkillReport skillReport) {
        FightSkillSetting skillSetting = skillStorage.get(skillReport.getSkillId(), true);
        if (skillSetting.getType() != SkillType.SPACE) {
            return;
        }

        HaiYaoGongZhuZSPassiveParam param = passiveState.getParam(HaiYaoGongZhuZSPassiveParam.class);

        if (param.getSummonOverlayTimes() <= 0) {
            return;
        }

        List<Unit> summons = context.getSummonUnits();
        if (summons == null || summons.isEmpty()) {
            return;
        }

        /* 给分身加自然庇护效果和被动 */
        for (Unit summon : summons) {
            if (summon.getSummonUnit() != owner) {
                continue;
            }
            BuffState buffState = BuffFactory.addBuff(param.getBuffId(), owner, summon, time, skillReport, null);
            buffState.setAddition(param.getSummonOverlayTimes());

            summon.addPassive(passiveState, owner);
        }
    }

    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time,
                       Context context, SkillState skillState, SkillReport skillReport) {
        HaiYaoGongZhuZSPassiveParam param = passiveState.getParam(HaiYaoGongZhuZSPassiveParam.class);
        BuffSetting buffSetting = buffStorage.get(param.getBuffId(), true);
        BuffState buffState = owner.getBuffStateByTag(buffSetting.getTag());
        final long hpChange = context.getHpChange(owner);

        if (buffState == null) {
            return;
        }

        int overlayTimes = buffState.getAddition(int.class);
        if (overlayTimes <= 0) {
            return;
        }

        double pct = Math.abs(hpChange) / 1.0 / owner.getValue(UnitValue.HP_MAX);

        if (pct < param.getImmunePct()) {
            return;
        }

        /* 免疫当前伤害 */
        buffState.setAddition(overlayTimes - 1);
        skillReport.add(time, owner.getId(), new Immune());
        PassiveUtils.hpUpdate(context, skillReport, owner, -hpChange, time);
    }

}
