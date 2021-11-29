package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Mp;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillReleasePassive;
import com.pangu.logic.module.battle.service.passive.param.LingHunZhenDangParam;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import com.pangu.logic.module.battle.service.skill.effect.HpMagicDamage;
import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 灵魂震荡：
 * 附近每有一个敌方英雄施放大招后，会受到灵魂震荡伤害，相当于灵魂摆渡人200%攻击力魔法伤害。
 * 2级：伤害提升至250%
 * 3级：每次触发时，自己获得200能量
 * 4级：伤害提升至300%
 */
@Component
public class LingHunZhenDang implements SkillReleasePassive {

    @Autowired
    private HpMagicDamage hpMagicDamage;

    @Override
    public PassiveType getType() {
        return PassiveType.LING_HUN_ZHEN_DANG;
    }

    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time, Context context, SkillReport skillReport) {
        if (skillState.getType() != SkillType.SPACE) {
            return;
        }
        // 忽视自己放大招
        if (owner.getFriend() == attacker.getFriend()) {
            return;
        }

        if (!attacker.canSelect(time)) {
            return;
        }

        LingHunZhenDangParam param = passiveState.getParam(LingHunZhenDangParam.class);
        EffectState effectState = new EffectState(null, 0);
        effectState.setParamOverride(new DamageParam(param.getFactor()));
        PassiveUtils.hpMagicDamage(hpMagicDamage, owner, attacker, skillState, effectState, time, context, skillReport, passiveState);

        int addMp = param.getAddMp();
        if (addMp <= 0) {
            return;
        }
        context.addPassiveValue(owner, AlterType.MP, addMp);
        skillReport.add(time, owner.getId(), new Mp(addMp));
    }
}
