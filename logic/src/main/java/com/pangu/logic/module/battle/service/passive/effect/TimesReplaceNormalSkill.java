package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillReleasePassive;
import com.pangu.logic.module.battle.service.passive.SkillSelectPassive;
import com.pangu.logic.module.battle.service.passive.param.RateReplaceNormalSkillParam;
import com.pangu.logic.module.battle.service.passive.param.TimesReplaceNormalSkillParam;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import com.pangu.framework.utils.math.RandomUtils;
import org.springframework.stereotype.Component;

/**
 * 导弹攻击
 * 每次第5下普攻会变成超目标发射一枚导弹，
 * ;
 * 普攻第几次会替换成另一个普攻
 */
@Component
public class TimesReplaceNormalSkill implements SkillSelectPassive, SkillReleasePassive {

    @Override
    public SkillState skillSelect(PassiveState passiveState, SkillState skillState, Unit owner, int time) {
        if (skillState.getType() != SkillType.NORMAL) {
            return null;
        }
        int times = passiveState.getAddition(Integer.class, 0);

        TimesReplaceNormalSkillParam param = passiveState.getParam(TimesReplaceNormalSkillParam.class);
        if (times < param.getTimes()) {
            return null;
        }
        passiveState.setAddition(0);
        return SkillFactory.initState(param.getSkillId());
    }

    @Override
    public PassiveType getType() {
        return PassiveType.TIMES_REPLACE_NORMAL_SKILL;
    }

    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time, Context context, SkillReport skillReport) {
        if (owner != attacker) {
            return;
        }
        if (skillState.getType() != SkillType.NORMAL) {
            return;
        }
        int times = passiveState.getAddition(Integer.class, 0);
        times += 1;
        final TimesReplaceNormalSkillParam param = passiveState.getParam(TimesReplaceNormalSkillParam.class);
        times = Math.min(param.getTimes(), times);
        passiveState.setAddition(times);
    }
}
