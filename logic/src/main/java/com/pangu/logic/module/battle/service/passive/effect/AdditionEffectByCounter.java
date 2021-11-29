package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Death;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackEndPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.AdditionalEffectByCounterParam;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 攻击时，根据目标身上指定buff的层数来执行不同操作
 */
@Component
public class AdditionEffectByCounter implements AttackEndPassive {
    @Override
    public void attackEnd(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final AdditionalEffectByCounterParam param = passiveState.getParam(AdditionalEffectByCounterParam.class);
        final String skillTag = skillState.getTag();
        boolean deBuff = false;
        boolean death = false;
        if (skillTag.equals(param.getDeBuffTriggerTag())) {
            deBuff = true;
        }
        if (skillTag.equals(param.getDeathTriggerTag())) {
            death = true;
        }
        if (!deBuff && !death) {
            return;
        }

        //  获取计数器
        final BuffState counter = target.getBuffStateByTag(param.getCounterTag());
        if (counter == null) {
            return;
        }
        final Integer count = counter.getAddition(Integer.class);
        if (count == null) {
            return;
        }

        //  根据当前释放的技能和计数器层数判断是否该触发额外效果
        final Map<String, Integer> skillTagToTriggerCount = param.getTriggerTagToTriggerCount();
        final Integer triggerCount = skillTagToTriggerCount.get(skillTag);
        if (triggerCount == null || count < triggerCount) {
            return;
        }

        if (deBuff) {
            for (int i = 0; i < count; i++) {
                BuffFactory.addBuff(param.getDeBuffId(), owner, target, time, skillReport, null);
            }
        }

        if (death) {
            target.foreverDead();
            final String targetId = target.getId();
            skillReport.add(time, targetId, new Death());

            final long hpChangeToDeath = -target.getValue(UnitValue.HP) - target.getValue(UnitValue.SHIELD) - context.getHpChange(target);
            context.addPassiveValue(target, AlterType.HP, hpChangeToDeath);
            skillReport.add(time, targetId, Hp.of(hpChangeToDeath));
        }
    }

    @Override
    public PassiveType getType() {
        return PassiveType.ADDITIONAL_EFFECT_BY_COUNTER;
    }
}
