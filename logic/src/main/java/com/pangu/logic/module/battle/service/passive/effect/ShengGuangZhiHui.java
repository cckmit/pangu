package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.model.report.values.Mp;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.alter.MpAlter;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.YuXueParam;
import org.springframework.stereotype.Component;

@Component
public class ShengGuangZhiHui implements AttackPassive {

    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (!context.isCrit(target)) {
            return;
        }
        passiveState.addCD(time);
        YuXueParam param = passiveState.getParam(YuXueParam.class);

        long addHp = (long) (param.getRecoverHpByAttackRate() * owner.getValue(UnitValue.HP_MAX));
        context.addPassiveValue(owner, AlterType.HP, addHp);
        PassiveValue passiveValue = PassiveValue.of(passiveState.getId(), owner.getId());
        passiveValue.add(Hp.of(addHp));
        skillReport.add(time, owner.getId(), passiveValue);

        if (param.getAddMp() <= 0) {
            return;
        }
        context.addPassiveValue(owner, AlterType.MP, param.getAddMp());
        passiveValue.add(new Mp(MpAlter.calMpChange(owner,param.getAddMp())));
    }

    @Override
    public PassiveType getType() {
        return PassiveType.SHENG_GUANG_ZHI_HUI;
    }
}
