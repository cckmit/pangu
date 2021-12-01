package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.BattleConstant;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.select.utils.BestCircle;
import com.pangu.logic.module.battle.service.select.select.utils.Circle;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.BaoFengJiangLinDamageParam;
import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 风暴女皇·艾琳技能：暴风降临
 * 1级：在敌方密集区域生成不断变大的旋风,期间对范围内的敌入造成5次每次30%魔法伤害随后发生一次风暴,造成110%攻击力的伤害
 * 2级：最后一次风暴结束后,范围内的敌入失去自身15%的能量
 * 3级：最后次风暴造成的伤害提升至140%攻击力
 * 4级：在沙暴天气下时,造成的伤害增加30%
 * @author Kubby
 */
@Component
public class BaoFengJiangLinDamage implements SkillEffect {

    @Autowired
    private HpMagicDamage magicDamage;
    @Autowired
    private MpChangePct mpChangePct;

    @Override
    public EffectType getType() {
        return EffectType.BAO_FENG_JIANG_LIN_DAMAGE;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time,
                        SkillState skillState, Context context) {
        BaoFengJiangLinDamageParam param = state.getParam(BaoFengJiangLinDamageParam.class);

        FixPointCircleDamageAddition addition = state.getAddition(
                FixPointCircleDamageAddition.class, new FixPointCircleDamageAddition());


        if (addition.point == null) {
            List<Point> enemyPoints = owner.getEnemy().getCurrent().stream().map(Unit::getPoint)
                    .collect(Collectors.toList());
            addition.point = BestCircle
                    .calBestPoint(owner.getPoint(), enemyPoints, param.getRadius(), BattleConstant.MAX_X,
                            BattleConstant.MAX_Y, false);
        }

        if (addition.point == null) {
            return;
        }

        boolean last = context.getLoopTimes() >= skillState.getExecuteTimes();

        Circle circle = new Circle(addition.point.getX(), addition.point.getY(), param.getRadius());

        EffectState effectState = new EffectState(null, 0);
        effectState.setParamOverride(new DamageParam(last ? param.getLastFactor() : param.getFactor()));


        for (Unit enemy : owner.getEnemy().getCurrent()) {
            if (circle.inShape(enemy.getPoint().getX(), enemy.getPoint().getY())) {
                magicDamage.execute(effectState, owner, enemy, skillReport, time, skillState, context);

                if (last && param.getLastMpLostPct() > 0) {
                    EffectState mpChangePctEffectState = new EffectState(null, 0);
                    mpChangePctEffectState.setParamOverride(param.getLastMpLostPct());
                    mpChangePct.execute(mpChangePctEffectState, owner, target, skillReport, time, skillState, context);
                }
            }
        }


        if (last) {
            state.setAddition(null);
        }
    }

    private static class FixPointCircleDamageAddition {

        Point point;

    }
}
