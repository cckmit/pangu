package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.UnitDiePassive;
import com.pangu.logic.module.battle.service.passive.param.RanHunParam;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import com.pangu.framework.utils.math.RandomUtils;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 顽劣之火·贝拉技能：燃魂
 * 1级：攻击带有点燃效果的目标时,有35%的概率使其眩晕1秒
 * 2级：概率提升至60%
 * 3级：眩晕时间提升至1.5秒
 * 4级：击杀掉带有点燃效果的目标时,额外恢复200点能量
 *
 * @author Kubby
 */
@Component
public class RanHun implements AttackPassive, UnitDiePassive {

    @Override
    public PassiveType getType() {
        return PassiveType.RAN_HUN;
    }

    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context,
                       SkillState skillState, SkillReport skillReport) {
        RanHunParam param = passiveState.getParam(RanHunParam.class);

        boolean burn = target.hasClassifyBuff(param.getBurnClassify());
        if (!burn) {
            return;
        }

        
        if (!RandomUtils.isHit(param.getTriggerRate())) {
            return;
        }
        PassiveUtils.addState(owner, target, UnitState.DISABLE, param.getDuration() + time, time, passiveState, context, skillReport);
    }

    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context,
                    ITimedDamageReport damageReport, Set<Unit> dieUnits) {
        if (owner != attacker) {
            return;
        }

        RanHunParam param = passiveState.getParam(RanHunParam.class);

        if (param.getMp() <= 0) {
            return;
        }

        long incMp = 0;

        for (Unit dieUnit : dieUnits) {
            boolean burn = dieUnit.hasClassifyBuff(param.getBurnClassify());
            if (burn) {
                incMp += param.getMp();
            }
        }

        if (incMp <= 0) {
            return;
        }

        context.addPassiveValue(owner, AlterType.MP, incMp);
        damageReport.add(time, owner.getId(),
                PassiveValue.single(passiveState.getId(), owner.getId(), new UnitValues(AlterType.MP, incMp)));
    }
}
