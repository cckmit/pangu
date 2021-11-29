package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.UnitRate;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.resource.FightSkillSetting;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.BeAttackBeforePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.WanLieZhiHuoZSParam;
import com.pangu.framework.resource.Storage;
import com.pangu.framework.resource.anno.Static;
import org.springframework.stereotype.Component;

/**
 * 顽劣之火·贝拉专属装备
 * 1：普攻暴击时候，点燃目标英雄，持续4秒，每秒造成40%魔法伤害
 * 10：普攻暴击时候，点燃目标英雄，持续4秒，每秒造成55%魔法伤害
 * 20：自己受到被点燃的单位的攻击时候，伤害降低12%
 * 30：自己受到被点燃的单位的攻击时候，伤害降低25%
 * @author Kubby
 */
@Component
public class WanLieZhiHuoZS implements BeAttackBeforePassive, AttackPassive {

    @Static
    private Storage<String, FightSkillSetting> skillStorage;

    @Override
    public PassiveType getType() {
        return PassiveType.WAN_LIE_ZHI_HUO_ZS;
    }

    @Override
    public void beAttackBefore(PassiveState passiveState, EffectState effectState, Unit owner, Unit attacker, int time,
                               Context context, SkillReport skillReport) {
        WanLieZhiHuoZSParam param = passiveState.getParam(WanLieZhiHuoZSParam.class);

        if (param.getRate() <= 0) {
            return;
        }

        boolean burn = attacker.hasClassifyBuff(param.getBurnClassify());
        if (!burn) {
            return;
        }

        owner.increaseRate(UnitRate.UNHARM_P, param.getRate());
        owner.increaseRate(UnitRate.UNHARM_M, param.getRate());

        passiveState.setAddition(burn);
    }

    @Override
    public void beAttackEnd(PassiveState passiveState, EffectState effectState, Unit owner, Unit attacker, int time,
                            Context context, SkillReport skillReport) {
        WanLieZhiHuoZSParam param = passiveState.getParam(WanLieZhiHuoZSParam.class);

        if (param.getRate() <= 0) {
            return;
        }

        boolean burn = passiveState.getAddition(boolean.class, false);
        if (!burn) {
            return;
        }

        owner.increaseRate(UnitRate.UNHARM_P, -param.getRate());
        owner.increaseRate(UnitRate.UNHARM_M, -param.getRate());

        passiveState.setAddition(null);
    }

    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context,
                       SkillState skillState, SkillReport skillReport) {
        FightSkillSetting skillSetting = skillStorage.get(skillReport.getSkillId(), true);
        if (skillSetting.getType() != SkillType.NORMAL) {
            return;
        }
        if (!context.isCrit(target)) {
            return;
        }

        WanLieZhiHuoZSParam param = passiveState.getParam(WanLieZhiHuoZSParam.class);

        BuffFactory.addBuff(param.getBuffId(), owner, target, time, skillReport, null);
    }

}
