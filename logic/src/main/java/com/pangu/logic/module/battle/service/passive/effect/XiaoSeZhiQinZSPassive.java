package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.ItemAdd;
import com.pangu.logic.module.battle.service.action.Action;
import com.pangu.logic.module.battle.service.action.EffectAction;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.UnitHpChangePassive;
import com.pangu.logic.module.battle.service.passive.param.XiaoSeZhiQinZSParam;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 1：开场自身周围生成3个乐章音符，可以对40%生命值以下的队友治疗25%最大生命，同一目标8秒间隔，消耗完毕后10秒再次生成
 * 10：可以对50%生命值以下的队友治疗35%最大生命，
 * 20：可以对60%生命值以下的队友治疗50%最大生命，
 * 30：受到治疗的队友+150能量
 */
@Component
public class XiaoSeZhiQinZSPassive implements UnitHpChangePassive {
    @Override
    public PassiveType getType() {
        return PassiveType.XIAO_SE_ZHI_QIN_ZS;
    }

    @Override
    public void hpChange(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> changeUnit) {
        final XiaoSeZhiQinZSParam param = passiveState.getParam(XiaoSeZhiQinZSParam.class);
        final LinkedList<ItemAdd> items = owner.getItems();
        if (CollectionUtils.isEmpty(items)) {
            return;
        }
        final Addition addition = getAddition(passiveState);
        final Map<Unit, Integer> unitHealedCd = addition.unitHealedCd;
        //筛选出生命值低于指定百分比且被治疗效果已cd的友方，并按生命值百分比进行排序后，取出音符数量个单位
        final List<Unit> unitsCanBeHealed = changeUnit.stream()
                .filter(unit -> !unit.isDead()
                        && unit.getValue(UnitValue.HP) > 0
                        && unit.getFriend() == owner.getFriend()
                        && unit.getHpPct() < param.getTriggerHpPct()
                        && (unitHealedCd.get(unit) == null || unitHealedCd.get(unit) < time)
                )
                .sorted((u1, u2) -> (int) (u1.getHpPct() - u2.getHpPct()))
                .limit(items.size())
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(unitsCanBeHealed)) {
            return;
        }
        //消耗道具
        final ItemAdd[] itemsToConsume = items.subList(0, unitsCanBeHealed.size()).toArray(new ItemAdd[0]);
        items.removeAll(Arrays.asList(itemsToConsume));
//        final String ownerId = owner.getId();
//        for (ItemAdd itemAdd : itemsToConsume) {
//            damageReport.add(time, ownerId, new ItemRemove(itemAdd.getId()));
//        }
        for (Unit unit : unitsCanBeHealed) {
            //回复生命
            final long hpChange = (long) (param.getMaxHpPctRecoverRate() * unit.getValue(UnitValue.HP_MAX));
            context.passiveRecover(owner, unit, hpChange, time, passiveState, damageReport);
            //回复能量
            PassiveUtils.mpUpdate(context, damageReport, owner, unit, param.getMp(), time, passiveState);
            //计算cd
            unitHealedCd.put(unit, time + param.getUnitRecoverCd());
        }
        if (!CollectionUtils.isEmpty(items)) {
            return;
        }

        //道具消耗完毕后，提交一个延迟生成道具的行为
        final Action itemGenAction = new Action() {
            private int actTime = time + param.getItemRegenerateCd();

            @Override
            public int getTime() {
                return actTime;
            }

            @Override
            public void execute() {
                final SkillState skillState = SkillFactory.initState(param.getItemGenerateSkill());
                final Unit target = owner.getTarget();
                SkillReport skillReport = SkillReport.sing(actTime, owner.getId(), skillState.getId(), skillState.getSingTime(), target != null && !target.isDead() ? target.getId() : null);
                owner.getBattle().addReport(skillReport);
                final EffectAction effectAction = new EffectAction(actTime, owner, skillState, skillReport, skillState.getEffectStates().get(0), Collections.singletonList(owner));
                owner.addTimedAction(effectAction);
            }
        };
        owner.addTimedAction(itemGenAction);

    }

    private Addition getAddition(PassiveState passiveState) {
        Addition addition = passiveState.getAddition(Addition.class);
        if (addition == null) {
            addition = new Addition();
            passiveState.setAddition(addition);
        }
        return addition;
    }

    private static class Addition {
        final private Map<Unit, Integer> unitHealedCd = new HashMap<>(3);
    }
}
