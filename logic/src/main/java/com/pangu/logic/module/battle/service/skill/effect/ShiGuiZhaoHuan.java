package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.*;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.SummonUnits;
import com.pangu.logic.module.battle.resource.EnemyUnitSetting;
import com.pangu.logic.module.battle.service.EnemyUnitReader;
import com.pangu.logic.module.battle.service.action.custom.SummonRemoveAction;
import com.pangu.logic.module.battle.service.core.*;
import com.pangu.logic.module.battle.service.passive.PassiveFactory;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.ShiGuiZhaoHuanParam;
import com.pangu.framework.utils.reflect.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 尸鬼召唤(3级)
 * 召唤食尸鬼（享有掘墓人80%的属性，不会受到伤害和控制，10秒后自动销售），食尸鬼会攻击掘墓人当前攻击的目标，攻击造成的伤害的20%会转化成生命值补给给掘墓人
 * 2级:目标每已损失1%生命值则侍女对目标的伤害提高1.2%
 * 3级:目标每已损失1%生命值则侍女对目标的伤害提高2.4%
 * 4级：若该目标死于侍女之手是，则掘墓额外获得500能量
 */
@Component
public class ShiGuiZhaoHuan implements SkillEffect {

    @Autowired
    private EnemyUnitReader unitSettingStorage;

    @Override
    public EffectType getType() {
        return EffectType.SHI_GUI_ZHAO_HUAN;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        Unit current = state.getAddition(Unit.class);
        if (current != null) {
            if (!current.isDead()) {
                return;
            }
        }
        ShiGuiZhaoHuanParam param = state.getParam(ShiGuiZhaoHuanParam.class);

        String unitId = param.getUnitId();
        EnemyUnitSetting enemyUnitSetting = unitSettingStorage.get(unitId, true);
        Fighter friend = owner.getFriend();
        int index = friend.nextSummonIndex();
        String id = Unit.toUnitId(friend.isAttacker(), index);
        Unit summonUnit = enemyUnitSetting.toUnit(id, null);
        // 复制属性
        double rate = param.getRate();

        HashMap<UnitValue, Long> values = owner.getValues();
        HashMap<UnitValue, Long> percentValue = percentValue(values, rate);
        summonUnit.getValues().putAll(percentValue);

//        HashMap<UnitRate, Double> rates = owner.getRates();
//        HashMap<UnitRate, Double> percentRate = percentRate(rates, rate);
//
//        summonUnit.setRates(percentRate);

        // 设置召唤者
        summonUnit.setSummonUnit(owner);
        summonUnit.setSummon(true);

        Point point = findValidPoint(owner.getPoint());
        summonUnit.setPoint(point);
        summonUnit.setFriend(owner.getFriend());
        summonUnit.setEnemy(owner.getEnemy());

        // 这个方法可以避免目标选择
        summonUnit.setJoinFighter(false);

        state.setAddition(summonUnit);
        context.addSummon(Collections.singletonList(summonUnit));
        skillReport.add(time, owner.getId(), new SummonUnits(Collections.singletonList(UnitInfo.valueOf(summonUnit))));

        int removeTime = param.getRemoveTime();
        //  无限期的召唤物在角色死亡时移除
        if (removeTime <= 0) {
            if (StringUtils.isEmpty(param.getRemovePassive())) {
                return;
            }
            PassiveState passiveState = PassiveFactory.initState(param.getRemovePassive(), time);
            passiveState.setAddition(new Unit[]{summonUnit});
            owner.addPassive(passiveState, owner);
            return;
        }
        owner.getBattle().addWorldAction(new SummonRemoveAction(time + removeTime, summonUnit, skillReport));
    }

    private Point findValidPoint(Point point) {
        return new Point(point.x + 10, point.y + 10);
    }

    private HashMap<UnitRate, Double> percentRate(HashMap<UnitRate, Double> rates, Double rate) {
        HashMap<UnitRate, Double> cpy = new HashMap<>(rates.size());
        for (Map.Entry<UnitRate, Double> entry : rates.entrySet()) {
            UnitRate k = entry.getKey();
            Double v = entry.getValue();
            cpy.put(k, v * rate);
        }
        return cpy;
    }

    private HashMap<UnitValue, Long> percentValue(HashMap<UnitValue, Long> values, Double rate) {
        HashMap<UnitValue, Long> cpy = new HashMap<>(values.size());
        UnitValue[] types = new UnitValue[]{UnitValue.HP_MAX, UnitValue.ATTACK_M, UnitValue.ATTACK_P, UnitValue.DEFENCE_M, UnitValue.DEFENCE_P};
        for (UnitValue type : types) {
            Long v = values.getOrDefault(type, 0L);
            cpy.put(type, (long) (v * rate));
        }
        Long hpMax = cpy.getOrDefault(UnitValue.HP_MAX, 10L);
        cpy.put(UnitValue.HP, hpMax);
        return cpy;
    }
}
