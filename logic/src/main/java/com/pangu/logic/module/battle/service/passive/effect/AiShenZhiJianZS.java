package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.OwnerDiePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillReleasePassive;
import com.pangu.logic.module.battle.service.passive.param.AiShenZhiJianZSParam;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

/**
 * 爱神之箭·波托斯专属装备
 * 1：队友释放大招后，自己普攻伤害+10%，最多2层
 * 10：自己普攻伤害+15%，最多2层
 * 20：自己普攻伤害+20%，最多2层
 * 30：自己普攻伤害+20%，最多3层
 * @author Kubby
 */
@Component
public class AiShenZhiJianZS implements SkillReleasePassive, OwnerDiePassive {

    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time,
                             Context context, SkillReport skillReport) {
        if (skillState.getType() != SkillType.SPACE) {
            return;
        }
        if (owner == attacker) {
            return;
        }
        if (owner.getFriend() != attacker.getFriend()) {
            return;
        }

        AiShenZhiJianZSParam param = passiveState.getParam(AiShenZhiJianZSParam.class);
        List<BuffState> buffStates = passiveState.getAddition(List.class, new LinkedList<>());

        if (buffStates.size() >= param.getOverlayLimit()) {
            return;
        }

        BuffState buffState = BuffFactory.addBuff(param.getBuffId(), owner, owner, time, skillReport, null);

        buffStates.add(buffState);
    }

    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attack, ITimedDamageReport timedDamageReport, int time,
                    Context context) {
        List<BuffState> buffStates = passiveState.getAddition(List.class);
        if (buffStates == null) {
            return;
        }
        for (BuffState buffState : buffStates) {
            BuffFactory.removeBuffState(buffState, owner, time);
        }
        passiveState.setAddition(null);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.AI_SHEN_ZHI_JIAN_ZS;
    }

}
