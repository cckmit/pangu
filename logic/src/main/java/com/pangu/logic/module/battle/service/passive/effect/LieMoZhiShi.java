package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.action.EffectAction;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.LieMoZhiShiParam;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class LieMoZhiShi implements AttackPassive {

    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (skillState.getType() != SkillType.NORMAL) return;
        final Addition addition = getAddition(passiveState);
        final LieMoZhiShiParam param = passiveState.getParam(LieMoZhiShiParam.class);
        addition.comboCount++;
        if (addition.comboCount == param.getComboCount()) {
            addition.comboCount = 0;
            //伤害加深
            long addDmg = -(long) (target.getValue(UnitValue.HP_MAX) * param.getHpRate());
            addDmg = Math.max(addDmg, -(long)param.getAtkFactorForMaxDmg()*owner.getValue(UnitValue.ATTACK_P));
            context.addPassiveValue(target, AlterType.HP, addDmg);
            skillReport.add(time, target.getId(), PassiveValue.single(passiveState.getId(), owner.getId(), Hp.of(addDmg)));
            //调用效果
            if (param.getEffectId() == null) return;
            final EffectState effectState = SkillFactory.getEffectState(param.getEffectId());
            final EffectAction effectAction = new EffectAction(time, owner, skillState, skillReport, effectState, Stream.of(target).collect(Collectors.toList()));
            owner.getBattle().addWorldAction(effectAction);
        }
    }

    private Addition getAddition(PassiveState passiveState) {
        Addition addition = passiveState.getAddition(Addition.class);
        if (addition == null) {
            addition = new Addition();
            passiveState.setAddition(addition);
        }
        return addition;
    }

    private static class Addition {
        private int comboCount;
    }

    @Override
    public PassiveType getType() {
        return PassiveType.LIE_MO_ZHI_SHI;
    }
}
