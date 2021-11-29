package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.passive.param.KuangYeXueMaiParam;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import org.springframework.stereotype.Component;

/**
 * 狂野血脉
 * 每次攻击时，自己会损失3%的当前生命值，但所造成的伤害增加5%。
 * 2级：所造成的伤害增加8%。
 * 3级：所造成的伤害增加12%
 * 4级：所造成的伤害增加15%
 */
@Component
public class KuangYeXueMai implements AttackPassive {
    @Override
    public PassiveType getType() {
        return PassiveType.KUANG_YE_XUE_MAI;
    }

    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        KuangYeXueMaiParam param = passiveState.getParam(KuangYeXueMaiParam.class);
        double hpDecrease = param.getHpDecrease();
        // 扣除当前生命3%
        final long hpCut = -(long) (hpDecrease * owner.getValue(UnitValue.HP));
        PassiveValue ownerPassiveValue = PassiveValue.single(passiveState.getId(), owner.getId(), Hp.of(hpCut));
        skillReport.add(time, owner.getId(), ownerPassiveValue);
        context.addPassiveValue(owner, AlterType.HP, hpCut);

        // 增加伤害5%
        long additionDamage = (long) (param.getAttackAddRate() * damage);
        PassiveValue targetPassiveValue = PassiveValue.single(passiveState.getId(), owner.getId(), Hp.of(additionDamage));
        skillReport.add(time, target.getId(), targetPassiveValue);
        context.addPassiveValue(target, AlterType.HP, additionDamage);

        passiveState.addCD(time);
    }
}
