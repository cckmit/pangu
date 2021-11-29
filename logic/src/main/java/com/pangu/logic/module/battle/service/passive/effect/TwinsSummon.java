package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.*;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.SummonUnits;
import com.pangu.logic.module.battle.resource.EnemyUnitSetting;
import com.pangu.logic.module.battle.service.BattleConstant;
import com.pangu.logic.module.battle.service.EnemyUnitReader;
import com.pangu.logic.module.battle.service.action.custom.ScheduledSkillUpdateAction;
import com.pangu.logic.module.battle.service.action.custom.SummonRemoveAction;
import com.pangu.logic.module.battle.service.core.*;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import com.pangu.logic.module.battle.service.skill.param.TwinsSummonParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * 当生命值低于20%时，魂体分裂，两人会同时出现在场上（被分裂出来的个体无法使用能量大招，且无法被攻击），持续10秒
 */
@Component
public class TwinsSummon implements SkillEffect {
    @Autowired
    private EnemyUnitReader enemyUnitReader;

    @Override
    public EffectType getType() {
        return EffectType.TWINS_SUMMON;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        Unit current = state.getAddition(Unit.class);
        if (current != null) {
            if (!current.isDead()) {
                return;
            }
        }
        TwinsSummonParam param = state.getParam(TwinsSummonParam.class);

        String unitId = param.getBaseId();
        EnemyUnitSetting enemyUnitSetting = enemyUnitReader.get(unitId, true);
        Fighter friend = owner.getFriend();
        int index = friend.nextSummonIndex();
        String id = Unit.toUnitId(friend.isAttacker(), index);
        Unit summonUnit = enemyUnitSetting.toUnit(id, null);
        summonUnit.setTransformState(param.getTransformState());

        //  复制属性
        double rate = param.getRate();

        HashMap<UnitValue, Long> values = owner.getValues();
        HashMap<UnitValue, Long> percentValue = percentValue(values, rate);
        summonUnit.getValues().putAll(percentValue);

        //  设置召唤者
        summonUnit.setSummonUnit(owner);
        summonUnit.setSummon(true);

        Point point = owner.getFriend().isAttacker() ? param.getPoint() : new Point(BattleConstant.MAX_X - param.getPoint().x, param.getPoint().y);
        summonUnit.setPoint(point);
        summonUnit.setFriend(owner.getFriend());
        summonUnit.setEnemy(owner.getEnemy());

        //  这个方法可以避免目标选择
        summonUnit.setJoinFighter(false);

        //  将指定技能添加到召唤物身上
        final List<SkillState> activeSkills = owner.getActiveSkills();
        final String skillPrefix = param.getSkillPrefix();
        for (SkillState activeSkill : activeSkills) {
            if (!activeSkill.getId().startsWith(skillPrefix)) {
                continue;
            }
            if (activeSkill.getType() == SkillType.SPACE) {
                if (param.getLastStrawDelay() > 0) {
                    final ScheduledSkillUpdateAction action = new ScheduledSkillUpdateAction(time + param.getLastStrawDelay(), SkillFactory.initState(activeSkill.getId()), summonUnit);
                    summonUnit.addTimedAction(action);
                }
                continue;
            }
            summonUnit.addActiveSkill(SkillFactory.initState(activeSkill.getId()));
        }

        state.setAddition(summonUnit);
        context.addSummon(Collections.singletonList(summonUnit));
        skillReport.add(time, owner.getId(), new SummonUnits(Collections.singletonList(UnitInfo.valueOf(summonUnit))));

        int removeTime = param.getRemoveTime();
        owner.getBattle().addWorldAction(new SummonRemoveAction(time + removeTime, summonUnit, skillReport));
    }

    private HashMap<UnitValue, Long> percentValue(HashMap<UnitValue, Long> values, Double rate) {
        HashMap<UnitValue, Long> cpy = new HashMap<>(values.size());
        UnitValue[] types = new UnitValue[]{UnitValue.HP_MAX, UnitValue.ATTACK_M, UnitValue.ATTACK_P, UnitValue.DEFENCE_M, UnitValue.DEFENCE_P, UnitValue.EP, UnitValue.EP_MAX};
        for (UnitValue type : types) {
            Long v = values.getOrDefault(type, 0L);
            cpy.put(type, (long) (v * rate));
        }
        Long hpMax = cpy.getOrDefault(UnitValue.HP_MAX, 10L);
        cpy.put(UnitValue.HP, hpMax);
        return cpy;
    }
}
