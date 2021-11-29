package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.XingBaoParam;
import com.pangu.logic.module.battle.service.passive.utils.SkillReportEditor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 星爆:
 * 开战后,每过7秒角色会指挥占星球,朝目标区域发射,对沿途目标造成130%魔法伤害(对后续目标伤害递减)指令结束后占星球会停在目标区域
 * 2级:伤害提升至145%
 * 3级:触发时间降低为5秒
 * 4级:伤害提升至160%
 * <p>
 * 此【被动】效果负责实现【每经过一个目标伤害递减的效果】
 * 其他效果由以下【主动】效果实现：
 * {@link com.pangu.logic.module.battle.service.skill.effect.XingBao}
 */
@Component("PASSIVE:XingBao")
@Deprecated
public class XingBao implements AttackPassive {
    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (skillState.getType() != SkillType.SKILL) return;
        if (!(context.getRootSkillEffectAction().getAddition() instanceof List)) return;
        if (damage >= 0) return;

        final XingBaoParam param = passiveState.getParam(XingBaoParam.class);
        //获取指定技能命中的目标数据
        final List<Unit> attackedUnits = context.getRootSkillEffectAction().getAddition(List.class);
        if (CollectionUtils.isEmpty(attackedUnits)) {
            return;
        }
        final int i = attackedUnits.indexOf(target);
        if (i < 0) {
            return;
        }

        final double decreaseFactor = Math.min((i * param.getDmgDecrFactorPerBar()), param.getMaxDmgDecrFactor());
        final long decreaseDmg = (long) (-decreaseFactor * damage);
        //扣减伤害，修改战报
        context.addPassiveValue(target, AlterType.HP, decreaseDmg);
        SkillReportEditor.editHpDamageReport(skillReport, target, decreaseDmg, time);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.XING_BAO;
    }
}
