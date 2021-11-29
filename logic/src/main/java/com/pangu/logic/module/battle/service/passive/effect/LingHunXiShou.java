package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.model.report.values.Mp;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.UnitDiePassive;
import com.pangu.logic.module.battle.service.passive.param.LingHunXiShouParam;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 灵魂吸收(2级)
 * 场上的非召唤物死亡时，尼汝吸收死者的灵魂恢复自身10%最大生命值与100点能量。
 * 2级:直至战斗结束自己的攻击与防御得到提升，提升数值为死者的5%（取战力最高的敌方英雄）
 * 3级:恢复自身20%最大生命值
 * 4级:直至战斗结束自己的攻击与防御得到提升，提升数值为死者的10% （取战力最高的敌方英雄）
 */
@Component
public class LingHunXiShou implements UnitDiePassive {

    @Override
    public PassiveType getType() {
        return PassiveType.LING_HUN_XI_SHOU;
    }

    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> dieUnits) {
        LingHunXiShouParam param = passiveState.getParam(LingHunXiShouParam.class);
        double hpRecoverRate = param.getHpRecoverRate();
        int mpAdd = param.getMp();
        long addHp = (long) (owner.getValue(UnitValue.HP_MAX) * hpRecoverRate);
        int count = 0;
        for (Unit target : dieUnits) {
            if (target.isSummon()) {
                continue;
            }

            context.addPassiveValue(owner, AlterType.HP, addHp);
            context.addPassiveValue(owner, AlterType.MP, mpAdd);
            count++;
        }
        PassiveValue passiveValue = PassiveValue.of(passiveState.getId(), owner.getId());
        if (count > 0) {
            passiveValue.add(Hp.fromRecover(addHp * count, false, false));
            passiveValue.add(new Mp(mpAdd * count));
            damageReport.add(time, owner.getId(), passiveValue);
        }

        double valueTakeRate = param.getRate();
        if (valueTakeRate <= 0) {
            return;
        }

        Unit targetUnit = null;
        long maxAttack = 0;
        for (Unit target : dieUnits) {
            if (target.isSummon()) {
                continue;
            }

            long attack = Math.max(target.getValue(UnitValue.ATTACK_P), target.getValue(UnitValue.ATTACK_M));
            if (attack >= maxAttack) {
                maxAttack = attack;
                targetUnit = target;
            }
        }
        if (targetUnit == null) {
            return;
        }

        PreAddValues addition = passiveState.getAddition(PreAddValues.class);
        if (addition != null) {
            if (addition.maxAttack >= maxAttack) {
                return;
            }
        }

        // 添加最新属性
        long attackM = (long) (targetUnit.getValue(UnitValue.ATTACK_M) * valueTakeRate);
        long attackP = (long) (targetUnit.getValue(UnitValue.ATTACK_P) * valueTakeRate);
        long defendM = (long) (targetUnit.getValue(UnitValue.DEFENCE_M) * valueTakeRate);
        long defendP = (long) (targetUnit.getValue(UnitValue.DEFENCE_P) * valueTakeRate);
        if (addition != null) {
            long amCh = attackM - addition.attackM;
            context.addPassiveValue(owner, AlterType.ATTACK_M, amCh);
            long apCh = attackP - addition.attackP;
            context.addPassiveValue(owner, AlterType.ATTACK_P, apCh);
            long dmCh = defendM - addition.defendM;
            context.addPassiveValue(owner, AlterType.DEFENCE_M, dmCh);
            long dpCh = defendP - addition.defendP;
            context.addPassiveValue(owner, AlterType.DEFENCE_P, dpCh);

            PreAddValues preAddValues = new PreAddValues(maxAttack, attackM, attackP, defendM, defendP);
            passiveState.setAddition(preAddValues);
            return;
        }
        context.addPassiveValue(owner, AlterType.ATTACK_M, attackM);
        context.addPassiveValue(owner, AlterType.ATTACK_P, attackP);
        context.addPassiveValue(owner, AlterType.DEFENCE_M, defendM);
        context.addPassiveValue(owner, AlterType.DEFENCE_P, defendP);

        PreAddValues preAddValues = new PreAddValues(maxAttack, attackM, attackP, defendM, defendP);
        passiveState.setAddition(preAddValues);
    }

    @AllArgsConstructor
    private static class PreAddValues {
        long maxAttack = -1;

        long attackM;
        long attackP;
        long defendM;
        long defendP;

    }
}
