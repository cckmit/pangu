package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.model.FilterType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.OwnerDiePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.SlayDmgShareParam;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 免疫致死伤害，并将该伤害的50%分摊给队友
 */
@Component
public class SlayDmgShare implements OwnerDiePassive {
    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attack, ITimedDamageReport timedDamageReport, int time, Context context) {
        final SlayDmgShareParam param = passiveState.getParam(SlayDmgShareParam.class);
        final boolean success = owner.revive(time);
        if (!success) {
            return;
        }

        //免疫致死伤害并回复伤害一定比例的生命值
        final long totalHpChange = context.getTotalHpChange(owner);
        final long value = owner.getValue(UnitValue.HP) + context.getHpChange(owner) + (long) (totalHpChange * param.getDmgRecoverRate());
        context.passiveRecover(owner, owner, -value, time, passiveState, timedDamageReport);

        //将伤害分摊给队友
        final List<Unit> friends = FilterType.FRIEND_WITHOUT_SELF.filter(owner, time);
        final int size = friends.size();

        final Number dmgToShare = totalHpChange * param.getDmgShareRate();
        for (Unit friend : friends) {
            PassiveUtils.hpUpdate(context, timedDamageReport, owner, friend, dmgToShare.longValue() / size, time, passiveState);
        }
        passiveState.addCD(time);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.SLAY_DMG_SHARE;
    }

    private static class Addition {
        /**
         * 上轮生命值
         */
        private Long preHp;
        /**
         * 本轮伤害
         */
        private long curDmg;
    }

}
