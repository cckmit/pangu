package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.JueJiangDeAiParam;
import org.springframework.stereotype.Component;

/**
 * 倔强的爱：
 * 血量低于75%后，获得30%攻击速度和20%免伤，持续7秒，1场战斗只会触发1次
 * 2级：血量低于50%后，会再获得1次该效果
 * 3级：血量低于25%后，再获得1次该效果
 * 4级：该效果持续10秒
 */
@Component
public class JueJiangDeAi implements DamagePassive {

    @Override
    public PassiveType getType() {
        return PassiveType.JUE_JIANG_DE_AI;
    }

    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time, Context context, SkillState skillState, SkillReport skillReport) {
        Integer addition = passiveState.getAddition(Integer.class);
        if (addition == null) {
            addition = 0;
        }
        JueJiangDeAiParam param = passiveState.getParam(JueJiangDeAiParam.class);
        // 每个阶段只生效一次
        int[] percentRange = param.getPercentRange();
        if (addition >= percentRange.length) {
            return;
        }
        long hpChange = context.getHpChange(owner);
        long curHp = owner.getValue(UnitValue.HP) + hpChange;
        long percent = curHp * 100 / owner.getValue(UnitValue.HP_MAX);
        if (percent > percentRange[addition]) {
            return;
        }
        String[] buffIds = param.getBuffIds();
        String addBuff = addition >= buffIds.length ? buffIds[buffIds.length - 1] : buffIds[addition];
        BuffFactory.addBuff(addBuff, owner, owner, time, skillReport, null);

        // 生效索引加1
        ++addition;
        passiveState.setAddition(addition);
    }
}
