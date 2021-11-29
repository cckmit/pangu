package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillReleasePassive;
import com.pangu.logic.module.battle.service.passive.param.ReleaseSkillAddBuffsParam;
import org.springframework.stereotype.Component;

/**
 * 其他人释放技能后添加属性
 */
@Component
public class ReleaseSkillAddBuffs implements SkillReleasePassive {
    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time, Context context, SkillReport skillReport) {
        if (skillState.getType() != SkillType.SPACE) {
            return;
        }
        ReleaseSkillAddBuffsParam param = passiveState.getParam(ReleaseSkillAddBuffsParam.class);
        switch (param.getType()) {
            case OWNER:
                if (owner != attacker) {
                    return;
                }
                break;
            case ENEMY:
                if (owner.getEnemy() == attacker.getEnemy()) {
                    return;
                }
                break;
            case FRIEND:
                if (owner.getFriend() != attacker.getFriend()) {
                    return;
                }
                break;
            default:
                return;

        }
        for (String s : param.getBuffs()) {
            BuffFactory.addBuff(s, owner, owner, time, skillReport, null);
        }
        passiveState.addCD(time);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.RELEASE_SKILL_ADD_BUFFS;
    }
}
