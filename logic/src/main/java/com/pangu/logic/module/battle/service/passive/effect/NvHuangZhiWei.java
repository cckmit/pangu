package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.NvHuangZhiWeiParam;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import org.springframework.stereotype.Component;

/**
 * 女皇之威:
 * 受到敌方武将技能伤害时,会将该将技能伤害的35%,反弹给敌人,冷却时长8秒
 * 2级:反弹的伤害提升至65%
 * 3级:冷却时间缩短至5秒
 * 4级:自身免疫被反弹部分的伤害
 */
@Component
public class NvHuangZhiWei implements DamagePassive {

    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (damage >= 0) {
            return;
        }
        if ((skillState.getType() != SkillType.SKILL) || (skillState.getType() != SkillType.SPACE)) {
            return;
        }
        if (owner == attacker) {
            return;
        }

        final NvHuangZhiWeiParam param = passiveState.getParam(NvHuangZhiWeiParam.class);
        final long hpChange = context.getHpChange(owner);
        //伤害反弹
        if (attacker.canSelect(time)) {
            final long dmgReflect = (long) (hpChange * param.getReflectFactor());
            PassiveUtils.hpUpdate(context, skillReport, owner, attacker, dmgReflect, time, passiveState);
        }
        //伤害减免
        if (param.getDmgCutFactor() <= 0) {
            return;
        }
        final long dmgCut = -(long) (hpChange * param.getDmgCutFactor());
        PassiveUtils.hpUpdate(context, skillReport, owner, dmgCut, time);

        passiveState.addCD(time);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.NV_HUANG_ZHI_WEI;
    }
}
