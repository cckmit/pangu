package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.PositionChange;
import com.pangu.logic.module.battle.service.BattleConstant;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.select.select.utils.TwoPointDistanceUtils;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import com.pangu.logic.module.battle.service.skill.param.ZhanZaiQianParam;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 斩风之息·武技能：斩在前
 * 1级：开战后,每隔10秒对随机一个敌人发动突进攻击,造成130%物理伤害；在突进的过程中免疫一切控制效果。25秒内无法对同一敌人重复突进
 * 2级：每次突进后，都会使下一次突进的基础伤害提升25%,最高可叠加到100%
 * 3级：触发间隔降低为8秒
 * 4级：每3次突进必定触发一次范围攻击，对目标周围的敌人造成相同伤害
 * @author Kubby
 */
@Component
public class ZhanZaiQian implements SkillEffect {

    @Autowired
    private HpPhysicsDamage physicsDamage;

    @Override
    public EffectType getType() {
        return EffectType.ZHAN_ZAI_QIAN;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time,
                        SkillState skillState, Context context) {
        ZhanZaiQianParam param = state.getParam(ZhanZaiQianParam.class);
        ZhanZaiQianAddition addition = state.getAddition(ZhanZaiQianAddition.class, new ZhanZaiQianAddition());

        addition.resetExec();



        List<Unit> units = new ArrayList<>(owner.getEnemy().getCurrent());
        Collections.shuffle(units);

        Unit beAttacker = null;
        for (Unit unit : units) {
            int tujinTime = addition.getTime(unit);
            if (tujinTime <= 0 || tujinTime + param.getTujinInterval() < time) {
                beAttacker = unit;
                break;
            }
        }

        if (beAttacker == null) {
            return;
        }


        addition.record(beAttacker, time);

        Set<Unit> beAttackers = new HashSet<>();
        beAttackers.add(beAttacker);


        if (addition.incTimes >= param.getRangeSelectTujinCount() && !StringUtils.isBlank(param.getRangeSelectId())) {
            List<Unit> extra = TargetSelector.select(beAttacker, param.getRangeSelectId(), time);
            beAttackers.addAll(extra);
            addition.incTimes = 0;
        }


        final Point ownerPoint = owner.getPoint();
        Point movePoint = TwoPointDistanceUtils
                .getNearEndPointDistance(ownerPoint, beAttacker.getPoint(), BattleConstant.SCOPE_HALF);
        owner.move(movePoint);
        skillReport.add(time, owner.getId(), PositionChange.of(ownerPoint.getX(), ownerPoint.getY()));


        EffectState effectState = new EffectState(null, 0);
        effectState.setParamOverride(new DamageParam(param.getFactor() * (1 + addition.dmgUpRate)));

        for (Unit unit : beAttackers) {
            physicsDamage.execute(effectState, owner, unit, skillReport, time, null, context);
        }


        addition.incTimes();


        if (param.getTujinDmgUpRate() > 0) {
            addition.dmgUpRate(param.getTujinDmgUpRate(), param.getTujinDmgUpRateLimit());
        }

        addition.exec();
    }

    private static class ZhanZaiQianAddition {


        double dmgUpRate;

        int incTimes;

        @Getter
        boolean exec;

        Map<Unit, Integer> times = new HashMap<>();

        void incTimes() {
            incTimes++;
        }

        void dmgUpRate(double inc, double max) {
            dmgUpRate += inc;
            dmgUpRate = Math.min(dmgUpRate, max);
        }

        void record(Unit unit, int time) {
            times.put(unit, time);
        }

        int getTime(Unit unit) {
            return times.getOrDefault(unit, -1);
        }

        void resetExec() {
            exec = false;
        }

        void exec() {
            exec = true;
        }

    }
}
