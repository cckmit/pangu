package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.utils.SkillReportEditor;
import org.springframework.stereotype.Component;


/**
 * 奥义！大地撕裂(3级)
 * 瑞贝卡凝聚全身的力量，召唤石刺，对正前方敌人造成250%攻击力的伤害，路径中心的敌人受到额外伤害。。
 * 2级:伤害提升至275%攻击力，且靠近岩石路径中心的敌人将受到1.5倍伤害
 * 3级:伤害提升至300%攻击力，且靠近岩石路径中心的敌人将受到2倍伤害
 * <p>
 * 该被动仅用于实现路径中轴增伤效果，主动效果由配表实现
 */
@Component("PASSIVE:DaDiSiLie")
public class DaDiSiLie implements AttackPassive {
    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (skillState.getType() != SkillType.SPACE) return;
        if (damage >= 0) return;

        final DaDiSiLieParam param = passiveState.getParam(DaDiSiLieParam.class);
        //计算目标与施法者在纵轴方向上的距离
        final int axisYDist = Math.abs(target.getPoint().y - owner.getPoint().y);
        //当该距离大于中轴宽度的一半时，不做任何处理
        if (axisYDist > param.getCentralAxisWidth() / 2) {
            return;
        }
        //位于中轴范围内时，增伤
        final long dmgUp = (long) (damage * (param.getDmgRate() - 1));
        context.addPassiveValue(target, AlterType.HP, dmgUp);
        SkillReportEditor.editHpDamageReport(skillReport, target, dmgUp, time);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.DA_DI_SI_LIE;
    }
}
