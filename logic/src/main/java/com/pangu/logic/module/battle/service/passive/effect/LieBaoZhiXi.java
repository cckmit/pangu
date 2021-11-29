package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.LieBaoZhiXiParam;
import org.springframework.stereotype.Component;

/**
 * 猎豹之息
 * 自身血量低于50%时，每降低5%的生命值，攻击速度提升1%。
 * 2级：生命值低于30%时，每降低5%的生命值，攻速提升2%
 * 3级：生命值低于30%时，每次攻击都会获得20%的吸血效果
 * 4级：生命值低于10%时，免疫一切控制效果
 */
@Component
public class LieBaoZhiXi implements AttackPassive, DamagePassive {
    @Override
    public PassiveType getType() {
        return PassiveType.LIE_BAO_ZHI_XI;
    }

    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        long currentHp = owner.getValue(UnitValue.HP);
        long hpMax = owner.getValue(UnitValue.HP_MAX);
        long hpPercent = currentHp * 100 / hpMax;
        LieBaoZhiXiParam param = passiveState.getParam(LieBaoZhiXiParam.class);
        if (hpPercent < param.getCanSuckHpRate()) {
            return;
        }
        long suckHp = -(long) (param.getSuckHp() * damage);
        if (suckHp <= 0) {
            return;
        }
        PassiveValue passiveValue = PassiveValue.single(passiveState.getId(), owner.getId(), Hp.fromRecover(suckHp));
        skillReport.add(time, owner.getId(), passiveValue);

        context.addPassiveValue(owner, AlterType.HP, suckHp);

        passiveState.addCD(time);
    }

    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time, Context context, SkillState skillState, SkillReport skillReport) {
        passiveState.addCD(time);

        LieBaoZhiXiParam param = passiveState.getParam(LieBaoZhiXiParam.class);
        int aPercent = param.getAaPercent();
        int aModel = param.getAaModel();
        int aValue = param.getAaValue();
        int bPercent = param.getBbPercent();
        int bModel = param.getBbModel();
        int bValue = param.getBbValue();

        int currentValue = 0;
        long currentHp = owner.getValue(UnitValue.HP);
        long hpMax = owner.getValue(UnitValue.HP_MAX);
        long hpPercent = currentHp * 100 / hpMax;
        if (hpPercent < aPercent) {
            // 直接低于30%
            if (bPercent > 0 && hpPercent < bPercent) {
                currentValue = (int) (aValue * (aPercent - bPercent) / aModel + bValue * (bPercent - hpPercent) / bModel);
            } else {
                currentValue = (int) (aValue * (aPercent - hpPercent) / aModel);
            }
        }

        AdditionValue preValue = passiveState.getAddition(AdditionValue.class);
        if (preValue == null) {
            preValue = new AdditionValue();
        }
        int diff = currentValue - preValue.preSpeedAdd;
        if (diff == 0) {
            return;
        }
        // 记录生效值
        preValue.preSpeedAdd = currentValue;
        passiveState.setAddition(preValue);

        double value = diff / 100.0;
        PassiveValue single = PassiveValue.single(passiveState.getId(), owner.getId(), new UnitValues(AlterType.RATE_NORMAL_SKILL_UP, value));
        skillReport.add(time, owner.getId(), single);
        context.addPassiveValue(owner, AlterType.RATE_NORMAL_SKILL_UP, value);

        int immuneRate = param.getImmuneRate();
        if (immuneRate > 0) {
            if (hpPercent < immuneRate) {
                if (!preValue.immune) {
                    preValue.immune = true;
                    owner.addState(UnitState.BA_TI);
                }
            } else {
                if (preValue.immune) {
                    preValue.immune = false;
                    owner.removeState(UnitState.BA_TI);
                }
            }
        }
    }

    private static class AdditionValue {
        // 之前记录保留的攻击加速
        private int preSpeedAdd;

        // 是否已经有免疫
        private boolean immune;
    }
}
