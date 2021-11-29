package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillReleasePassive;
import com.pangu.logic.module.battle.service.passive.param.YeXiaoZhiRenZSParam;
import org.springframework.stereotype.Component;

/**
 * 夜枭之刃·艾莲娜专属装备
 * 1：每次使用技能使自己的位置发生变化后,将提升300点闪避,持续5秒,该增益效果可以叠加多次，每次单独计算持续时间
 * 10：每次使用技能使自己的位置发生变化后,将提升400点闪避,持续5秒,该增益效果可以叠加多次，每次单独计算持续时间
 * 20：每次使用技能使自己的位置发生变化后,将提升500点闪避,持续5秒,该增益效果可以叠加多次，每次单独计算持续时间
 * 30：每次使用技能使自己的位置发生变化后,将提升600点闪避,持续5秒,该增益效果可以叠加多次，每次单独计算持续时间
 * @author Kubby
 */
@Component
public class YeXiaoZhiRenZS implements SkillReleasePassive {

    @Override
    public PassiveType getType() {
        return PassiveType.YE_XIAO_ZHI_REN_ZS;
    }

    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time,
                             Context context, SkillReport skillReport) {
        if (owner != attacker) {
            return;
        }
        YeXiaoZhiRenZSParam param = passiveState.getParam(YeXiaoZhiRenZSParam.class);
        if (!param.getSkillTags().contains(skillState.getTag())) {
            return;
        }
        BuffFactory.addBuff(param.getBuffId(), owner, owner, time, skillReport, null);
    }
}
