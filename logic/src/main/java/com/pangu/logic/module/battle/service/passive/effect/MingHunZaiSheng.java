package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.DispelType;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.OwnerDiePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.MingHunZaiShengParam;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 冥魂再生
 * 死亡后，短暂延时后复活，并恢复最大生命值50%的生命。每场战斗只能触发一次。
 * 2级:恢复生命值提升至60%最大生命值。
 * 3级:复活时使附近的敌人眩晕3秒。
 * 4级:恢复生命值提升至75%最大生命值。
 */
@Component
public class MingHunZaiSheng implements OwnerDiePassive {
    @Override
    public PassiveType getType() {
        return PassiveType.MING_HUN_ZAI_SHENG;
    }


    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attack, ITimedDamageReport timedDamageReport, int time, Context context) {
        final long value = owner.getValue(UnitValue.HP) + context.getHpChange(owner);
        final boolean success = owner.revive(time);
        if (!success) {
            return;
        }
        // 保留1点血
        context.addPassiveValue(owner, AlterType.HP, -value + 1);

//        timedDamageReport.add(time, owner.getId(), Hp.of(-value + 1));

        MingHunZaiShengParam param = passiveState.getParam(MingHunZaiShengParam.class);

        // 添加不可目标选择状态
        int aliveDelay = param.getAliveDelay();
        owner.addState(UnitState.WU_DI, aliveDelay + time);
        owner.addState(UnitState.BA_TI, aliveDelay + time);

        String skillId = param.getSkillId();
        SkillFactory.updateNextExecuteSkill(time, owner, skillId);
        passiveState.addCD(time);
    }
}
