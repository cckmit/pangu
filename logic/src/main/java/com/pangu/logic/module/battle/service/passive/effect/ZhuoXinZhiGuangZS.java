package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.ZhuoXinZhiGuangParamZS;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import org.springframework.stereotype.Component;

/**
 * 1：自身普攻和技能造成伤害时，敌人能量越高，自身额外伤害比例越高，最高20%（每100能量，2%）
 * 10：最高35%（每100能量，3.5%）
 * 20：最高50%（每100能量，5%）
 * 30：最高70%（每100能量，7%）
 */
@Component
public class ZhuoXinZhiGuangZS implements AttackPassive {
    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (damage>=0) {
            return;
        }
        final ZhuoXinZhiGuangParamZS param = passiveState.getParam(ZhuoXinZhiGuangParamZS.class);
        final double totalDmgUpRate = target.getValue(UnitValue.MP) / 100.0 * param.getDmgUpRatePer100Mp();
        final double trueDmgUpRate = Math.min(totalDmgUpRate, param.getMaxDmgUpRate());
        final long dmgUp = (long) (trueDmgUpRate * damage);
        PassiveUtils.hpUpdate(context, skillReport, owner, target, dmgUp, time, passiveState);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.ZHUO_XIN_ZHI_GUANG_ZS;
    }
}
