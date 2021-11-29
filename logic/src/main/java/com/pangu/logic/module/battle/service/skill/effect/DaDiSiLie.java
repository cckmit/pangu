package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.select.utils.BestRectangle;
import com.pangu.logic.module.battle.service.select.select.utils.Rectangle;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.DaDiSiLieParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 奥义！大地撕裂(3级)
 * 瑞贝卡凝聚全身的力量，召唤石刺，对区域内敌人造成250%攻击力的伤害，路径中心的敌人受到额外伤害。。
 * 2级:伤害提升至275%攻击力，且靠近岩石路径中心的敌人将受到1.5倍伤害
 * 3级:伤害提升至300%攻击力，且靠近岩石路径中心的敌人将受到2倍伤害
 * <p>
 * 因策划调整技能，该效果暂时弃用
 */
@Component
@Deprecated
public class DaDiSiLie implements SkillEffect {
    @Autowired
    private HpMagicDamage magicDamage;

    @Override
    public EffectType getType() {
        return EffectType.DA_DI_SI_LIE;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final DaDiSiLieParam param = state.getParam(DaDiSiLieParam.class);
        //计算出最佳矩形
        final BestRectangle.UnitInfo info = BestRectangle.calBestRectangle(owner, owner.getEnemy().getCurrent(), param.getWidth(), param.getLength());
        if (info == null) {
            return;
        }
        //对最佳矩形内的单位造成伤害
        final List<Unit> inRect = info.getInRect();
        for (Unit unit : inRect) {
            state.setParamOverride(param.getDmg());
            magicDamage.execute(state, owner, unit, skillReport, time, skillState, context);
            state.setParamOverride(null);
        }
        //获取最佳矩形中轴矩形
        final Rectangle innerRect = new Rectangle(owner.getPoint(), info.getTarget().getPoint(), param.getInnerWidth(), param.getLength());

        final ArrayList<Unit> inInnerRect = new ArrayList<>();
        //缓存中轴矩形内部的单位
        for (Unit unit : inRect) {
            if (innerRect.inRect(unit.getPoint().x, unit.getPoint().y)) inInnerRect.add(unit);
        }

        context.getRootSkillEffectAction().setAddition(inInnerRect);
    }
}
