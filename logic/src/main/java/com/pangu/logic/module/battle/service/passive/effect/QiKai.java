package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.resource.FightSkillSetting;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillReleasePassive;
import com.pangu.logic.module.battle.service.passive.param.QiKaiParam;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.framework.resource.Storage;
import com.pangu.framework.resource.anno.Static;
import com.pangu.framework.utils.math.RandomUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Kubby
 */
@Component
public class QiKai implements DamagePassive, SkillReleasePassive {

    @Static
    private Storage<String, FightSkillSetting> skillStorage;

    @Override
    public PassiveType getType() {
        return PassiveType.QI_KAI;
    }

    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time,
                       Context context, SkillState skillState, SkillReport skillReport) {
        //  达到充能次数，直接触发，无视触发条件。否则走正常触发逻辑
        final Integer chargeTimes = passiveState.getAddition(Integer.class, 0);
        final QiKaiParam param = passiveState.getParam(QiKaiParam.class);
        if (param.getTriggerNormalAtkTimes() <= chargeTimes) {
            passiveState.setAddition(0);
        } else if (skillState.getType() != SkillType.NORMAL || !RandomUtils.isHit(param.getRate())) {
            return;
        }


        List<Unit> targets = TargetSelector.select(owner, param.getSelectId(), time);

        long targetDamage = (long) (-owner.getValue(UnitValue.HP_MAX) * param.getDamagePct());


        if (param.getDmgLimit() > 0) {
            long maxDamage = (long) (-owner.getValue(UnitValue.ATTACK_P) * param.getDmgLimit());
            targetDamage = Math.max(targetDamage, maxDamage);
        }

        if (targetDamage >= 0) {
            targetDamage = -1;
        }


        for (Unit target : targets) {
            context.addPassiveValue(target, AlterType.HP, targetDamage);
            skillReport.add(time, target.getId(),
                    PassiveValue.single(passiveState.getId(), owner.getId(), new UnitValues(AlterType.HP, targetDamage)));

            final String deBuff = param.getDeBuff();
            final List<BuffState> buffStates = target.getBuffBySettingId(deBuff);
            if (buffStates.size() < param.getDeBuffLimit()) {
                BuffFactory.addBuff(deBuff, owner, target, time, skillReport, null);
            }
        }
    }

    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time, Context context, SkillReport skillReport) {
        if (owner != attacker) {
            return;
        }
        if (skillState.getType() != SkillType.NORMAL) {
            return;
        }
        final Integer preCount = passiveState.getAddition(Integer.class, 0);
        passiveState.setAddition(preCount + 1);
    }
}
