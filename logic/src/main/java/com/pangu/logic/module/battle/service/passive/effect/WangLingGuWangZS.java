package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.UnitRate;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.resource.FightSkillSetting;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackBeforePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillReleasePassive;
import com.pangu.logic.module.battle.service.passive.param.WangLingGuWangZSParam;
import com.pangu.framework.resource.Storage;
import com.pangu.framework.resource.anno.Static;
import org.springframework.stereotype.Component;

/**
 * 亡灵骨王·克劳狄斯专属装备
 * 1：每次释放骨王之怒，获得自己最大生命值30%护盾
 * 10：骨王之怒必定命中
 * 20：骨王之怒必定暴击
 * 30：每次释放骨王之怒，获得自己最大生命值60%护盾
 * @author Kubby
 */
@Component
public class WangLingGuWangZS implements SkillReleasePassive, AttackBeforePassive {

    @Static
    private Storage<String, FightSkillSetting> skillStorage;

    @Override
    public PassiveType getType() {
        return PassiveType.WANG_LING_GU_WANG_ZS;
    }

    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time,
                             Context context, SkillReport skillReport) {
        if (owner != attacker) {
            return;
        }
        WangLingGuWangZSParam param = passiveState.getParam(WangLingGuWangZSParam.class);
        if (!param.getSkillTag().equals(skillState.getTag())) {
            return;
        }
        BuffFactory.addBuff(param.getBuffId(), owner, owner, time, skillReport, null);
    }

    @Override
    public void attackBefore(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time,
                             Context context, SkillReport skillReport) {
        FightSkillSetting skillSetting = skillStorage.get(skillReport.getSkillId(), true);
        WangLingGuWangZSParam param = passiveState.getParam(WangLingGuWangZSParam.class);
        if (!param.getSkillTag().equals(skillSetting.getTag())) {
            return;
        }
        if (param.isHit()) {
            owner.increaseRate(UnitRate.HIT, Integer.MAX_VALUE);
        }
        if (param.isCrit()) {
            owner.increaseRate(UnitRate.CRIT, Integer.MAX_VALUE);
        }
    }

    @Override
    public void attackEnd(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time,
                          Context context, SkillReport skillReport) {
        FightSkillSetting skillSetting = skillStorage.get(skillReport.getSkillId(), true);
        WangLingGuWangZSParam param = passiveState.getParam(WangLingGuWangZSParam.class);
        if (!param.getSkillTag().equals(skillSetting.getTag())) {
            return;
        }
        if (param.isHit()) {
            owner.increaseRate(UnitRate.HIT, -Integer.MAX_VALUE);
        }
        if (param.isCrit()) {
            owner.increaseRate(UnitRate.CRIT, -Integer.MAX_VALUE);
        }
    }

}
