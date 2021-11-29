package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.LieYanTianHuoParam;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 根据目标身上指定Counter层数，提供额外增伤，并为自己添加减伤BUFF
 */
@Component
public class LieYanTianHuo implements AttackPassive {
    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (skillState.getType() != SkillType.SPACE) {
            return;
        }
        final LieYanTianHuoParam param = passiveState.getParam(LieYanTianHuoParam.class);

        // 火焰计数器校验
        final String CONTEXT_ADDITION_NAME = owner.getId() + ":" + passiveState.getId();
        final BuffState buffState = target.getBuffStateByTag(param.getCounterBuffTag());
        if (buffState == null) {
            return;
        }
        final Integer count = buffState.getAddition(Integer.class, 0);
        if (count <= 0) {
            return;
        }
        //  清空目标火焰层数
//        buffState.setAddition(0);

        //  根据目标火焰层数增伤
        PassiveUtils.hpUpdate(context, skillReport, target, (long) (damage * param.getDmgDeepenRate() * count), time);

        //  添加目标火焰层数的BUFF，不可超过上限值。
        final List<BuffState> buffStates = owner.getBuffByClassify(param.getBuffClassify());
        final Integer preBuffCount = context.getAddition(CONTEXT_ADDITION_NAME, buffStates.size());
        final int buffCountCanAdd = param.getBuffMaxCount() - preBuffCount;
        final int buffCountToAdd = Math.min(buffCountCanAdd, count);
        if (buffCountToAdd <= 0) {
            return;
        }
        for (int i = 0; i < buffCountToAdd; i++) {
            BuffFactory.addBuff(param.getBuffId(), owner, owner, time, skillReport, null);
        }
        context.setAddition(CONTEXT_ADDITION_NAME, preBuffCount + buffCountToAdd);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.LIE_YAN_TIAN_HUO;
    }
}
