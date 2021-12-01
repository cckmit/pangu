package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.UnitRate;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.resource.FightSkillSetting;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillReleasePassive;
import com.pangu.logic.module.battle.service.passive.param.ChuanHuoParam;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.skill.effect.HpMagicDamage;
import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import com.pangu.framework.resource.Storage;
import com.pangu.framework.resource.anno.Static;
import com.pangu.framework.utils.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 顽劣之火·贝拉技能：传火
 * 1级：普通攻击有25%概率造成火焰爆炸,对目标及其周围敌人额外成80%魔法伤害,每5秒触发1次
 * 2级：触发的概率提升至45%
 * 3级：伤害提升至115%
 * 4级：该次攻击暴击率额外提升20%
 * @author Kubby
 */
@Component
public class ChuanHuo implements SkillReleasePassive {

    @Static
    private Storage<String, FightSkillSetting> skillStorage;

    @Autowired
    private HpMagicDamage hpMagicDamage;

    @Override
    public PassiveType getType() {
        return PassiveType.CHUAN_HUO;
    }

    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time,
                             Context context, SkillReport skillReport) {
        if (owner != attacker) {
            return;
        }
        if (skillState.getType() != SkillType.NORMAL) {
            return;
        }
        ChuanHuoParam param = passiveState.getParam(ChuanHuoParam.class);

        if (!RandomUtils.isHit(param.getRate())) {
            return;
        }


        List<Unit> targetUnits = TargetSelector.select(owner, param.getSelectId(), time);


        EffectState damageEffectState = new EffectState(null, 0);
        damageEffectState.setParamOverride(new DamageParam(param.getFactor()));

        if (param.getCritUpRate() > 0) {
            owner.increaseRate(UnitRate.CRIT, param.getCritUpRate());
        }

        for (Unit target : targetUnits) {
            hpMagicDamage.execute(damageEffectState, owner, target, skillReport, time, null, context);
        }

        if (param.getCritUpRate() > 0) {
            owner.increaseRate(UnitRate.CRIT, -param.getCritUpRate());
        }

        passiveState.addCD(time);
    }

}
