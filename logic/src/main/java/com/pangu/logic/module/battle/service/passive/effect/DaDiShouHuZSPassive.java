package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Mp;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackBeforePassive;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillReleasePassive;
import com.pangu.logic.module.battle.service.passive.param.DaDiShouHuZSPassiveParam;
import org.springframework.stereotype.Component;

/**
 * 大地守护·安泰专属装备
 * 1：每嘲讽一个敌人获得40能量，并嘲讽期间降低被嘲讽敌人10%攻击力
 * 10：每嘲讽一个敌人获得60能量，并嘲讽期间降低被嘲讽敌人15%攻击力
 * 20：拥有护盾期间，每秒回复3%最大生命值血量
 * 30：拥有护盾期间，每秒回复4%最大生命值血量，且负面BUFF时间-50%
 *
 * @author Kubby
 */
@Component
public class DaDiShouHuZSPassive implements SkillReleasePassive, AttackBeforePassive, DamagePassive {

    @Override
    public PassiveType getType() {
        return PassiveType.DA_DI_SHOU_HU_ZS;
    }

    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time,
                             Context context, SkillReport skillReport) {
        if (owner != attacker) {
            return;
        }

        /* 释放图腾守护时（会产生护盾），添加关联的BUFF */
        DaDiShouHuZSPassiveParam param = passiveState.getParam(DaDiShouHuZSPassiveParam.class);
        if (param.getSkillTag() != null && param.getSkillTag().equals(skillState.getTag())) {
            passiveState.setAddition(true);
            for (String buffId : param.getBuffIds()) {
                BuffFactory.addBuff(buffId, owner, owner, time, skillReport, null);
            }
            return;
        }

        boolean active = passiveState.getAddition(boolean.class, false);

        /* 在已释放图腾守护的情况下（会产生护盾），释放大招时，添加关联的BUFF */
        if (skillState.getType() == SkillType.SPACE && active) {
            for (String buffId : param.getBuffIds()) {
                BuffFactory.addBuff(buffId, owner, owner, time, skillReport, null);
            }
        }
    }

    @Override
    public void attackBefore(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time,
                             Context context, SkillReport skillReport) {

    }

    @Override
    public void attackEnd(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time,
                          Context context, SkillReport skillReport) {
        if (effectState.getType() != EffectType.SNEER) {
            return;
        }

        DaDiShouHuZSPassiveParam param = passiveState.getParam(DaDiShouHuZSPassiveParam.class);

        /* 嘲讽时，自身恢复能量 */
        int mpRecover = param.getSneerMpRecover();
        context.addPassiveValue(owner, AlterType.MP, mpRecover);
        skillReport.add(time, owner.getId(), PassiveValue.single(passiveState.getId(), owner.getId(), new Mp(mpRecover)));

        /* 嘲讽时，给对方添加BUFF */
        BuffFactory.addBuff(param.getSneerBuffId(), owner, target, time, skillReport, null);
    }

    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time,
                       Context context, SkillState skillState, SkillReport skillReport) {
        long hpChange = context.getHpChange(owner);
        if (hpChange > 0) {
            return;
        }
        if (owner.getValue(UnitValue.SHIELD) > -hpChange) {
            return;
        }

        DaDiShouHuZSPassiveParam param = passiveState.getParam(DaDiShouHuZSPassiveParam.class);

        boolean active = passiveState.getAddition(boolean.class, false);

        /* 无护盾时，删除关联的BUFF */
        if (active) {
            for (String buffId : param.getBuffIds()) {
                for (BuffState buffState : owner.getBuffBySettingId(buffId)) {
                    BuffFactory.removeBuffState(buffState, owner, time);
                }
            }
        }
    }

}
