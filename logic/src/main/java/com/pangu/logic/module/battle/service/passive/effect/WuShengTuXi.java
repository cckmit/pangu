package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.passive.param.WuShengTuXiParam;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.model.report.values.PositionChange;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import com.pangu.logic.module.battle.service.select.select.utils.TwoPointDistanceUtils;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 无声突袭(4级)
 * 每10秒一次，受到伤害时，将完全躲避该次伤害。并瞬身至攻击者身后反击敌人，造成150%攻击力的伤害
 * 2级:伤害提升至170%攻击力
 * 3级:反击后，获得30%攻速加成，持续4秒
 * 4级:触发间隔缩短至8秒
 */
@Component
public class WuShengTuXi implements DamagePassive {
    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time, Context context, SkillState skillState, SkillReport skillReport) {
        // 躲避伤害
        final long hpChange = context.getHpChange(owner);
        if (hpChange >= 0) {
            return;
        }
        if (owner == attacker) {
            return;
        }
        if (!owner.canFightBack(time)) {
            return;
        }
        if (!attacker.canSelect(time)) {
            return;
        }
        if (owner.getFriend() == attacker.getFriend()) {
            return;
        }

        passiveState.addCD(time);

        PassiveUtils.hpUpdate(context, skillReport, owner, -hpChange, time);

        PassiveValue passiveValue = PassiveValue.of(passiveState.getId(), owner.getId());
        skillReport.add(time, owner.getId(), passiveValue);


        WuShengTuXiParam param = passiveState.getParam(WuShengTuXiParam.class);
        String buff = param.getBuff();
        if (StringUtils.isNotEmpty(buff)) {
            BuffFactory.addBuff(buff, owner, owner, time, skillReport, null);
        }

        owner.setTarget(attacker);
        // 改变自身位置到敌人背后
        final Point ownerPoint = owner.getPoint();
        Point validPoint = TwoPointDistanceUtils.getNearEndPointDistance(ownerPoint, attacker.getPoint(), param.getDist());
        owner.move(validPoint);

        passiveValue.add(PositionChange.of(ownerPoint.x, ownerPoint.y));

        String skillId = param.getSkillId();
        SkillFactory.updateNextExecuteSkill(time, owner, skillId);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.WU_SHENG_TU_XI;
    }
}
