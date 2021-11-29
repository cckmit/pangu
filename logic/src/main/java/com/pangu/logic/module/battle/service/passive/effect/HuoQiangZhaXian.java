package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.action.SkillAction;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.HuoQiangZhaXianParam;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import org.springframework.stereotype.Component;

/**
 * 陆海霸主·巴达克技能：枪火乍现
 * 1级：暴击会触发一次额外的攻击,造成140%伤害,冷却3秒
 * 2级：伤害提升至165%
 * 3级：伤害提升至190%
 * 4级：冷却时间降低为2秒
 * @author Kubby
 */
@Component
public class HuoQiangZhaXian implements AttackPassive {

    @Override
    public PassiveType getType() {
        return PassiveType.HUO_QIANG_ZHA_XIAN;
    }

    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context,
                       SkillState skillState, SkillReport skillReport) {
        if (!context.isCrit(target)) {
            return;
        }

        HuoQiangZhaXianParam param = passiveState.getParam(HuoQiangZhaXianParam.class);

        if (owner.getAction() instanceof SkillAction
                && ((SkillAction)owner.getAction()).getSkillState().getId().equals(param.getSkillId())) {
            return;
        }

        SkillFactory.updateNextExecuteSkill(time, owner, param.getSkillId());

        passiveState.addCD(time);
    }

}
