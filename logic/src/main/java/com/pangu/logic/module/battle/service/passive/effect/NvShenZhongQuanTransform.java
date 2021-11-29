package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.NvShenZhongQuanTransformParam;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import org.springframework.stereotype.Component;

/**
 * 召唤的每只狗在进行了8次攻击后将会变大。变大后的狗造成的伤害提升100%且被献祭时梅希拉生命回复量提高80%。
 */
@Component
public class NvShenZhongQuanTransform implements AttackPassive {
    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final NvShenZhongQuanTransformParam param = passiveState.getParam(NvShenZhongQuanTransformParam.class);
        Integer count = passiveState.getAddition(Integer.class, 0);
        if (count >= param.getTriggerAtkTimes()) {
            return;
        }

        ++count;
        final int triggerAtkTimes = param.getTriggerAtkTimes();
        if (count == triggerAtkTimes) {
            SkillFactory.updateNextExecuteSkill(time, owner, param.getSkillId());
        }
        passiveState.setAddition(count);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.NV_SHEN_ZHONG_QUAN_TRANSFORM;
    }
}
