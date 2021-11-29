package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.UnitRate;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.resource.FightSkillSetting;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackBeforePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillReleasePassive;
import com.pangu.framework.resource.Storage;
import com.pangu.framework.resource.anno.Static;
import org.springframework.stereotype.Component;

/**
 * 恶魔猎人·康斯坦丁专属装备
 * 1：进入战斗后，自身攻击力+10%
 * 10：进入战斗后，自身攻击力+20%
 * 20：自身或周围友军释放必杀技时，自身下次普攻必定暴击
 * 30：进入战斗后，自身攻击力+30%
 * @author Kubby
 */
@Component
public class EMoLieRenZS implements SkillReleasePassive, AttackBeforePassive {

    @Static
    private Storage<String, FightSkillSetting> skillStorage;

    @Override
    public PassiveType getType() {
        return PassiveType.E_MO_LIE_REN_ZS;
    }

    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time,
                             Context context, SkillReport skillReport) {
        if (skillState.getType() != SkillType.SPACE) {
            return;
        }
        if (owner.getFriend() != attacker.getFriend()) {
            return;
        }

        int rangeRequire = passiveState.getParam(int.class);

        if (owner != attacker && owner.getPoint().distance(attacker.getPoint()) > rangeRequire) {
            return;
        }

        passiveState.setAddition(true);
    }

    @Override
    public void attackBefore(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time,
                             Context context, SkillReport skillReport) {
        FightSkillSetting skillSetting = skillStorage.get(skillReport.getSkillId(), true);
        if (skillSetting.getType() != SkillType.NORMAL) {
            return;
        }
        boolean crit = passiveState.getAddition(boolean.class, false);
        if (!crit) {
            return;
        }
        owner.increaseRate(UnitRate.CRIT, 999999);
    }

    @Override
    public void attackEnd(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time,
                          Context context, SkillReport skillReport) {
        FightSkillSetting skillSetting = skillStorage.get(skillReport.getSkillId(), true);
        if (skillSetting.getType() != SkillType.NORMAL) {
            return;
        }
        boolean crit = passiveState.getAddition(boolean.class, false);
        if (!crit) {
            return;
        }
        owner.increaseRate(UnitRate.CRIT, -999999);

        passiveState.setAddition(false);
    }
}
