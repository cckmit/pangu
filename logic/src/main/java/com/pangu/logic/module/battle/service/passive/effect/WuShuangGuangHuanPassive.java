package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.resource.BuffSetting;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.UnitDiePassive;
import com.pangu.logic.module.battle.service.passive.param.WuShuangGuangHuanPassiveParam;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 使周围敌方英雄的防御降低10%
 * 用于周期性判断是否有英雄进入自己周围
 * 无双光环：
 * 使周围敌方英雄的防御降低10%。光环影响下的敌方英雄阵亡后，无双战姬将获得阵亡武将的10%攻击力加成，持续5秒
 * 2级：获得阵亡武将的15%攻击力加成
 * 3级：使周围敌方英雄的防御降低15%
 * 4级：敌方英雄阵亡时回复自身10%最大生命值的血量
 */
@Component
public class WuShuangGuangHuanPassive implements UnitDiePassive {

    @Override
    public PassiveType getType() {
        return PassiveType.WU_SHUANG_GUANG_HUAN;
    }

    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> dieUnits) {
        WuShuangGuangHuanPassiveParam param = passiveState.getParam(WuShuangGuangHuanPassiveParam.class);
        String buffCheck = param.getBuffCheck();
        BuffSetting setting = BuffFactory.getSetting(buffCheck);
        String tag = setting.getTag();
        String selfAddBuff = param.getSelfAddBuff();
        long addHp = (long) (owner.getOriginValue(UnitValue.HP) * param.getRecoverHpRate());
        PassiveValue passiveValue = null;
        for (Unit unit : dieUnits) {
            if (unit.isSummon()) {
                continue;
            }
            BuffState buffState = unit.getBuffStateByTag(tag);
            if (buffState == null) {
                continue;
            }
            BuffFactory.addBuff(selfAddBuff, unit, owner, time, damageReport, null);
            if (passiveValue == null) {
                passiveValue = PassiveValue.of(passiveState.getId(), owner.getId());
            }
            passiveValue.add(Hp.of(addHp));
        }
        if (passiveValue != null) {
            damageReport.add(time, owner.getId(), passiveValue);
        }
    }
}
