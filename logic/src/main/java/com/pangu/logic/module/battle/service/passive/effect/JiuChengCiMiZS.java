package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.UnitRate;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.*;
import com.pangu.logic.module.battle.service.passive.param.JiuChengCiMiZSParam;
import org.springframework.stereotype.Component;

/**
 * 1：贯穿射击命中的目标会附带一层4秒破甲效果，减少24%防御
 * 10：贯穿射击后4秒内攻速+10%
 * 20：贯穿射击后4秒内攻速+15%
 * 30：对攻击拥有被穿甲的目标，必定暴击
 */
@Component
public class JiuChengCiMiZS implements AttackBeforePassive, AttackPassive, SkillReleasePassive {
    //目标持有指定buff，必定会被暴击
    @Override
    public void attackBefore(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time, Context context, SkillReport skillReport) {
        final JiuChengCiMiZSParam param = passiveState.getParam(JiuChengCiMiZSParam.class);
        if (!param.isDeBuffBonus()) {
            return;
        }
        final BuffState buffState = target.getBuffStateByTag("jiu_cheng_ci_mi_zs_debuff");
        if (buffState == null) {
            return;
        }
        final boolean typeCorrect = effectState.getType() == EffectType.HP_M_DAMAGE || effectState.getType() == EffectType.HP_P_DAMAGE;
        if (!typeCorrect) {
            return;
        }
        owner.increaseRate(UnitRate.CRIT, param.getCritUpFactor());
        passiveState.setAddition(true);
    }

    //回复正常暴击率
    @Override
    public void attackEnd(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time, Context context, SkillReport skillReport) {
        final Boolean triggered = passiveState.getAddition(Boolean.class);
        if (triggered == null) {
            return;
        }
        final JiuChengCiMiZSParam param = passiveState.getParam(JiuChengCiMiZSParam.class);
        owner.increaseRate(UnitRate.CRIT, -param.getCritUpFactor());
        passiveState.setAddition(null);
    }

    //为攻击的目标添加debuff
    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (damage >= 0) {
            return;
        }
        if (skillState.getType() != SkillType.SKILL) {
            return;
        }
        final JiuChengCiMiZSParam param = passiveState.getParam(JiuChengCiMiZSParam.class);
        BuffFactory.addBuff(param.getDeBuff(), owner, target, time, skillReport, null);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.JIU_CHENG_CI_MI_ZS;
    }

    //释放指定技能后自身获得增益
    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time, Context context, SkillReport skillReport) {
        if (owner != attacker) {
            return;
        }
        if (skillState.getType() != SkillType.SKILL) {
            return;
        }
        final JiuChengCiMiZSParam param = passiveState.getParam(JiuChengCiMiZSParam.class);
        BuffFactory.addBuff(param.getBuff(), owner, owner, time, skillReport, null);
    }


}
