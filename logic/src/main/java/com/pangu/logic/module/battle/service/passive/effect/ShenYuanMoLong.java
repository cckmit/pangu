package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.Battle;
import com.pangu.logic.module.battle.service.action.custom.TransformEndAction;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.*;
import com.pangu.logic.module.battle.service.passive.param.ShenYuanMoLongParam;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 龙形被动：替换技能组，击杀延长时间，所有攻击无视魔抗
 */
@Component
public class ShenYuanMoLong implements SkillSelectPassive, UnitDiePassive, AttackBeforePassive, AttackPassive {
    @Override
    public PassiveType getType() {
        return PassiveType.SHEN_YUAN_MO_LONG;
    }

    //被动持续期间，维持龙型替换普攻和技能
    @Override
    public SkillState skillSelect(PassiveState passiveState, SkillState skillState, Unit owner, int time) {
        final ShenYuanMoLongParam param = passiveState.getParam(ShenYuanMoLongParam.class);
        switch (skillState.getType()) {
            case SKILL:
                passiveState.addCD(time);
                return SkillFactory.initState(param.getSkillId());
            case NORMAL:
                passiveState.addCD(time);
                return SkillFactory.initState(param.getNormalId());
            default:
                return null;
        }
    }

    //龙型状态下，无法释放大招。击杀目标延长被动持续时间和大招cd时间
    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> dieUnits) {
        final ShenYuanMoLongParam param = passiveState.getParam(ShenYuanMoLongParam.class);
        if (param.getDelay() <= 0) return;
        //校验击杀者是否为自己
        if (owner != attacker || dieUnits.size() <= 0) return;
        //延长变身时间
        final Battle battle = owner.getBattle();
        final List<TransformEndAction> transformEndActions = battle.getWorldActions().stream()
                .filter(act -> act instanceof TransformEndAction && ((TransformEndAction) act).getTarget() == owner)
                .map(act -> (TransformEndAction) act).collect(Collectors.toList());
        transformEndActions.forEach(act -> {
            act.setTime(act.getTime() + param.getDelay());
            battle.worldActionUpdate(act);
        });

        //延长大招cd
        owner.getActiveSkills().stream()
                .filter(skillState -> skillState.getType() == SkillType.SPACE)
                .forEach(skillState -> skillState.setCd(skillState.getCd() + param.getDelay()));

        passiveState.addCD(time);
    }

    //攻击前降低对方魔防，攻击后恢复：只属于自己的防御无视
    //给对方添加一个buff，持续时间内降低治疗效果
    @Override
    public void attackBefore(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time, Context context, SkillReport skillReport) {
        final ShenYuanMoLongParam param = passiveState.getParam(ShenYuanMoLongParam.class);
        DefenceChange additon = getAddition(passiveState);
        long originDefence = target.getOriginValue(UnitValue.DEFENCE_M);
        final long defenceChange = -(long) (param.getDefenceIgnore() * originDefence);
        target.increaseValue(UnitValue.DEFENCE_M, defenceChange);
        additon.defenceChange = defenceChange;
    }


    private static class DefenceChange {
        private long defenceChange;
    }

    private DefenceChange getAddition(PassiveState passiveState) {
        DefenceChange addition = passiveState.getAddition(DefenceChange.class);
        if (addition == null) {
            addition = new DefenceChange();
            passiveState.setAddition(addition);
        }
        return addition;
    }

    @Override
    public void attackEnd(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time, Context context, SkillReport skillReport) {
        DefenceChange addition = getAddition(passiveState);
        target.increaseValue(UnitValue.DEFENCE_M, -addition.defenceChange);
        passiveState.addCD(time);
    }

    //攻击添加debuff
    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final ShenYuanMoLongParam param = passiveState.getParam(ShenYuanMoLongParam.class);
        final BuffState buffState = BuffFactory.addBuff(param.getDebuff(), owner, target, time, skillReport, null);
        if (buffState == null) return;
        passiveState.addCD(time);
    }
}
