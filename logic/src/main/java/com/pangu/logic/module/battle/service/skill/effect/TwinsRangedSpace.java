package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.*;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.model.report.values.Mp;
import com.pangu.logic.module.battle.model.report.values.PositionChange;
import com.pangu.logic.module.battle.service.BattleConstant;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.select.utils.TwoPointDistanceUtils;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.TwinsRangedSpaceParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * "召唤星辰施放持续数秒的星辰。星辰能量流转不息，对范围内敌人造成300%魔法攻击力的伤害，随着自身星辰之力的大小会额外附加不同的效果：
 * 当星辰之力大于30时，对处于眩晕状态的敌人造成的伤害伤害提升10%；
 * 当星辰之力大于50时，使受到伤害的敌人能量降低20%；
 * 当星辰之力大于80时，对处于大招范围的敌人造成微弱的牵引，限制敌方向外方移动
 * 2级：对处于眩晕状态的敌人造成的伤害伤害提升30%
 * 3级：使受到伤害的敌人能量降低40%
 * 4级：对处于大招范围的敌人造成微弱的牵引的范围增加"
 */
@Component
public class TwinsRangedSpace implements SkillEffect {
    @Autowired
    private HpMagicDamage magicDamage;

    @Override
    public EffectType getType() {
        return EffectType.TWINS_RANGED_SPACE;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final TwinsRangedSpaceParam param = state.getParam(TwinsRangedSpaceParam.class);
        //  造成伤害
        state.setParamOverride(param.getDmgParam());
        magicDamage.execute(state, owner, target, skillReport, time, skillState, context);
        state.setParamOverride(null);

        //  对沉默单位增伤
        if (owner.getValue(UnitValue.EP) > param.getDmgEp() && target.hasState(UnitState.SILENT, time)) {
            long hpChange = (long) (context.getHpChange(target) * param.getDmgRate());
            context.addValue(target, AlterType.HP, hpChange);
            skillReport.add(time, target.getId(), Hp.of(hpChange));
        }

        //  能量减少
        if (owner.getValue(UnitValue.EP) > param.getMpCutEp()) {
            final long mpChange = -(long) (target.getValue(UnitValue.MP) * param.getMpCutPct());
            context.addValue(target, AlterType.MP, mpChange);
            skillReport.add(time, target.getId(), new Mp(mpChange));
        }

        //  将目标往屏幕中央牵引
        if (owner.getValue(UnitValue.EP) > param.getDragEp()) {
            Point center = new Point(BattleConstant.MAX_X / 2, BattleConstant.MAX_Y / 2);
            Point unitPoint = target.getPoint();
            int curDistance = unitPoint.distance(center);
            Point targetPosition;
            final int modifier = BattleConstant.SCOPE_HALF;
            if (curDistance <= modifier) {
                targetPosition = unitPoint;
            } else {
                int dragDistance = param.getDragDist();
                if (param.getDragDist() >= curDistance) {
                    dragDistance = curDistance - modifier;
                }
                targetPosition = TwoPointDistanceUtils.getNearStartPoint(unitPoint, dragDistance, center);
            }
            target.move(targetPosition);
            skillReport.add(time, target.getId(), PositionChange.of(unitPoint.x, unitPoint.y));
            target.addState(UnitState.NO_MOVE, param.getDragDur() + time);
        }
    }
}
