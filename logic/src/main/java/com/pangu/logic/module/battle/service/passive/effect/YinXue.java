package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.YinXueParam;
import org.springframework.stereotype.Component;


@Component
public class YinXue implements AttackPassive {
    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final YinXueParam param = passiveState.getParam(YinXueParam.class);
        //非使用指定技能，不进行恢复
        if (!param.getSkillId().equals(skillState.getId())) return;
        //未造成伤害，不进行恢复
        if (damage >= 0) return;
        //计算回复量
        final long lossHp = owner.getValue(UnitValue.HP_MAX) - owner.getValue(UnitValue.HP);
        final int recover = (int) Math.ceil(lossHp * param.getRate() * context.getTargetAmount());
        context.addPassiveValue(owner, AlterType.HP, recover);
        // 生成战报信息
        final PassiveValue pv = PassiveValue.single(passiveState.getId(), owner.getId(), Hp.of(recover));
        skillReport.add(time, owner.getId(), pv);

        passiveState.addCD(time);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.YIN_XUE;
    }
}
