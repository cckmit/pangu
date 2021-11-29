package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.FilterType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import com.pangu.logic.module.battle.service.skill.param.AttackParam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 月刃裁决
 * 召唤月之刃攻击敌方审判印记最多的英雄,造成250%攻击力的伤害并给对方施加1层印记；当施放时目标有3层以上审判印记额外眩晕敌人1.5秒；5层审判印记时,清除敌人的审判印记,并使断罪审判的伤害提高300%
 * 2级:伤害提升至280%攻击力
 * 3级:伤害提升至310%攻击力
 * 4级:眩晕时间提升至2.5秒
 *
 * 此【主动】效果负责【筛选目标并执行伤害】
 * 其余效果由相关【被动】实现
 * {@link com.pangu.logic.module.battle.service.passive.effect.YueRenCaiJue}
 */
@Component
public class YueRenCaiJue implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.YUE_REN_CAI_JUE;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final AttackParam param = state.getParam(AttackParam.class);
        final EffectType attackType = param.getAttackType();
        if (attackType != EffectType.HP_M_DAMAGE && attackType != EffectType.HP_P_DAMAGE) {
            return;
        }

        //筛选出印记最多的目标
        final List<Unit> enemies = FilterType.ENEMY.filter(owner, time);
        final List<Unit> sortedEnemies = enemies.stream()
                .map(unit -> {
                    final BuffState shen_pan_yin_ji = unit.getBuffStateByTag("shen_pan_yin_ji");
                    int markCount = 0;
                    int distance = owner.getPoint().distance(unit.getPoint());
                    if (shen_pan_yin_ji != null) markCount = shen_pan_yin_ji.getAddition(Integer.class);
                    return new UnitRef(markCount, distance, unit);
                })
                //印记数量相同则按距离从小到大进行排序
                .sorted(Comparator.comparing(UnitRef::getMarkCount, Comparator.reverseOrder()).thenComparing(UnitRef::getDistance))
                .map(ref -> ref.unit)
                .collect(Collectors.toList());
        if (sortedEnemies.isEmpty()) {
            return;
        }
        final Unit trueTarget = sortedEnemies.get(0);
        //对目标造成伤害
        state.setParamOverride(param.getDmg());
        final SkillEffect hpDmgEffect = SkillFactory.getSkillEffect(attackType);
        hpDmgEffect.execute(state, owner, trueTarget, skillReport, time, skillState, context);
        state.setParamOverride(null);
    }


    /**
     * 排序对象
     */
    @AllArgsConstructor
    @Getter
    private static class UnitRef {
        //目标身上的印记数量
        private final int markCount;

        //目标距离施法者的距离
        private final int distance;

        //目标
        private final Unit unit;
    }
}
