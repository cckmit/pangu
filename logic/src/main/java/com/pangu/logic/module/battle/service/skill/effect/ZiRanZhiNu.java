package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.ZiRanZhiNuParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 自然之怒(3级)
 * 歌林召唤自然之神的愤怒，以随机顺序召唤木刺，在敌人脚下凸起，依次打击每一个敌入，每次造成150%攻击力的魔法伤害，最多打击5次，会多次打击同一个目标。
 * 2级:现在会多次打击同一个目标
 * 3级:每次打击的伤害将比上次提升10%攻击力
 */
@Component
public class ZiRanZhiNu implements SkillEffect {
    @Autowired
    private HpMagicDamage magicDamage;

    @Override
    public EffectType getType() {
        return EffectType.ZI_RAN_ZHI_NU;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        //不使用给定的target作为目标，注意此类技能配表时只能配置单个目标以防重复执行
        final ZiRanZhiNuParam param = state.getParam(ZiRanZhiNuParam.class);
        //随机选取一名目标
        final List<Unit> enemies = owner.getEnemy().getCurrent();
        //不可对同一目标造成伤害时，排除已造成伤害的目标
        final Object addition = context.getRootSkillEffectAction().getAddition();
        Map<Unit, Integer> attackedMap;
        if (addition instanceof Map) {
            attackedMap = (Map) addition;
        } else {
            attackedMap = new HashMap<>();
            context.getRootSkillEffectAction().setAddition(attackedMap);
        }
        //过滤出本轮循环未选定过的目标
        final List<Unit> selectableUnits = new ArrayList<>();
        if (!param.isRepeatable()) {
            selectableUnits.addAll(
                    enemies.stream()
                            .filter(enemy -> !(attackedMap.containsKey(enemy) && attackedMap.get(enemy) > 0))
                            .collect(Collectors.toList())
            );
        } else {
            selectableUnits.addAll(enemies);
        }
        //若不存在可选目标，不执行任何效果
        if (selectableUnits.size() <= 0) return;
        //从可选目标中随机抽取指定个单元作为本轮攻击的目标
        Collections.shuffle(selectableUnits);
        final List<Unit> targets = selectableUnits.subList(0, param.getCount());
        //将本轮攻击的目标统计到skillState中
        targets.forEach(t -> {
            Integer atkTimes = attackedMap.get(t);
            if (atkTimes == null) atkTimes = 0;
            atkTimes++;
            attackedMap.put(t, atkTimes);
        });
        //执行伤害
        state.setParamOverride(param.getDmgParam());
        for (Unit trueTarget : targets) {
            magicDamage.execute(state, owner, trueTarget, skillReport, time, skillState, context);
        }
        state.setParamOverride(null);
    }
}
