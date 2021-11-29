package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillReleasePassive;
import com.pangu.logic.module.battle.service.passive.param.AddBuffWhenAttackParam;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.framework.utils.math.RandomUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 自身释放技能时添加buff
 */
@Component
public class AddBuffWhenSkillRelease implements SkillReleasePassive {
    @Override
    public PassiveType getType() {
        return PassiveType.ADD_BUFF_WHEN_SKILL_RELEASE;
    }

    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time, Context context, SkillReport skillReport) {
        //非自身释放技能，不执行逻辑
        if (owner != attacker) {
            return;
        }

        final AddBuffWhenAttackParam param = passiveState.getParam(AddBuffWhenAttackParam.class);
        //默认全部技能类型都添加buff
        if (param.getTypes() != null) {
            final boolean triggered = Arrays.stream(param.getTypes()).anyMatch(type -> type == skillState.getType());
            if (!triggered) return;
        }
        final double rateIncrease = param.getRateIncrease();
        final AdditionalParam addition = getAddition(passiveState);
        //触发后才执行逻辑
        boolean triggered;
        if (param.getMaxTrigger() <= 0) {
            triggered = RandomUtils.isHit(addition.acTriggerRate);
        } else {
            triggered = RandomUtils.isHit(addition.acTriggerRate) && addition.currentTrigger < param.getMaxTrigger();
        }
        if (!triggered) {
            addition.acTriggerRate += rateIncrease;
            return;
        }
        //触发后重置基础概率
        addition.acTriggerRate = param.getBaseRate();
        addition.currentTrigger++;
        final String[] buffs = param.getBuffs();
        final String targetId = param.getTargetId();
        List<Unit> buffAddTargets = new ArrayList<>();
        //未配置targetId默认为自己添加Buff
        if (targetId == null) {
            buffAddTargets.add(owner);
        } else {
            buffAddTargets.addAll(TargetSelector.select(owner, targetId, time));
        }

        //为每个目标添加每个BUFF
        for (Unit buffAddTarget : buffAddTargets) {
            for (String buff : buffs) {
                BuffFactory.addBuff(buff, owner, buffAddTarget, time, skillReport, null);
            }
        }

        passiveState.addCD(time);
    }

    private AdditionalParam getAddition(PassiveState passiveState) {
        AdditionalParam addition = passiveState.getAddition(AdditionalParam.class);
        if (addition == null) {
            addition = new AdditionalParam();
            addition.acTriggerRate = passiveState.getParam(AddBuffWhenAttackParam.class).getBaseRate();
            passiveState.setAddition(addition);
        }
        return addition;
    }

    private static class AdditionalParam {
        //用于统计累计触发率
        private double acTriggerRate;
        //用于统计buff成功添加次数
        private int currentTrigger;
    }
}
