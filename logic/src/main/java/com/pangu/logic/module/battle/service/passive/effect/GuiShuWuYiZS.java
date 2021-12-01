package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.model.UnitInfo;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.model.report.values.SummonUnits;
import com.pangu.logic.module.battle.resource.EnemyUnitSetting;
import com.pangu.logic.module.battle.service.EnemyUnitReader;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.*;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillReleasePassive;
import com.pangu.logic.module.battle.service.passive.param.GuiShuWuYiZSParam;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.skill.effect.HpMagicDamage;
import com.pangu.logic.module.battle.service.skill.effect.Sneer;
import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import com.pangu.logic.module.battle.service.skill.param.StateAddParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * 诡术巫医·奥尔萨专属装备
 * 1：释放巫医图腾的时候，会额外在敌人后方生成一个嘲讽2秒的图腾，拥有650%攻击力的血量，嘲讽图腾造成120%范围伤害
 * 10：嘲讽图腾每秒回复10%最大生命值
 * 20：嘲讽图腾造成150%范围伤害
 * 30：嘲讽图腾每秒恢复15%最大生命值
 *
 * @author Kubby
 */
@Component
public class GuiShuWuYiZS implements SkillReleasePassive {

    @Autowired
    private Sneer sneer;
    @Autowired
    private HpMagicDamage hpMagicDamage;

    @Autowired
    private EnemyUnitReader enemyUnitReader;

    @Override
    public PassiveType getType() {
        return PassiveType.GUI_SHU_WU_YI_ZS;
    }

    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time,
                             Context context, SkillReport skillReport) {
        if (owner != attacker) {
            return;
        }
        if (owner.getTarget() == null) {
            return;
        }
        GuiShuWuYiZSParam param = passiveState.getParam(GuiShuWuYiZSParam.class);
        if (!param.getSkillTags().contains(skillState.getTag())) {
            return;
        }

        
        String unitId = param.getUnitId();
        EnemyUnitSetting enemyUnitSetting = enemyUnitReader.get(unitId, true);
        Fighter friend = owner.getFriend();
        int index = friend.nextSummonIndex();
        String id = Unit.toUnitId(friend.isAttacker(), index);
        Unit summonUnit = enemyUnitSetting.toUnit(id, null);

        HashMap<UnitValue, Long> values = summonUnit.getValues();
        long hp = (long) (owner.getValue(UnitValue.ATTACK_M) * param.getHpRate());
        hp = Math.max(hp, 1);

        values.put(UnitValue.HP, hp);
        values.put(UnitValue.HP_MAX, hp);

        summonUnit.setSummonUnit(owner);
        summonUnit.setSummon(true);

        Point point = findValidPoint(owner.getTarget().getPoint());
        summonUnit.setPoint(point);
        summonUnit.setFriend(owner.getFriend());
        summonUnit.setEnemy(owner.getEnemy());

        summonUnit.setJoinFighter(true);

        context.addSummon(Collections.singletonList(summonUnit));
        PassiveValue passiveValue = PassiveValue
                .single(passiveState.getId(), owner.getId(), new SummonUnits(Collections.singletonList(UnitInfo.valueOf(summonUnit))));
        skillReport.add(time, owner.getId(), passiveValue);

        
        List<Unit> targetUnits = TargetSelector.select(summonUnit, param.getSelectId(), time);

        
        EffectState damageEffectState = new EffectState(null, 0);
        damageEffectState.setParamOverride(new DamageParam(param.getFactor()));
        for (Unit target : targetUnits) {
            hpMagicDamage.execute(damageEffectState, owner, target, skillReport, time, null, context);
        }

        
        EffectState sneerEffectState = new EffectState(null, 0);
        StateAddParam stateAddParam = new StateAddParam();
        stateAddParam.setState(UnitState.SNEER);
        stateAddParam.setTime(param.getSneerTime());
        sneerEffectState.setParamOverride(stateAddParam);
        for (Unit target : targetUnits) {
            sneer.execute(sneerEffectState, summonUnit, target, skillReport, time, null, context);
        }

        
        if (!StringUtils.isBlank(param.getCureBuffId())) {
            BuffFactory.addBuff(param.getCureBuffId(), summonUnit, summonUnit, time, skillReport, null);
        }
    }

    private Point findValidPoint(Point point) {
        return new Point(point.x + 10, point.y + 10);
    }
}
