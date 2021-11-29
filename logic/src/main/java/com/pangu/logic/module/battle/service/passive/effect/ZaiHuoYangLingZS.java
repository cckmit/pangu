package com.pangu.logic.module.battle.service.passive.effect;


import com.pangu.logic.module.battle.model.FilterType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 1：战斗开始时,获得:200点能量
 * 10：若场上有被噩运标记的敌人,自身受到伤害的25%将由被噩运标记的敌人承受
 * 20：战斗开始时,获得500点能量
 * 30：若场上有被灵魂标记的敌人。自身受到伤害的40%将由被噩运标记的敌人承受
 */
@Component
public class ZaiHuoYangLingZS implements DamagePassive {
    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (damage >= 0) {
            return;
        }
        final long hpChange = context.getHpChange(owner);
        final Double apportionRate = passiveState.getParam(Double.class);
        final List<Unit> enemies = FilterType.ENEMY.filter(owner, time);
        final List<Unit> marked = new ArrayList<>();
        for (Unit enemy : enemies) {
            if (enemy.getBuffStateByTag("e_yun_jiang_zhi") != null) {
                marked.add(enemy);
            }
        }
        if (marked.isEmpty()) {
            return;
        }
        //回复被分摊的比例
        final long dmgDecr = -(long) (hpChange * apportionRate);
        PassiveUtils.hpUpdate(context, skillReport, owner, dmgDecr, time);
        //将免伤部分转移给被标记目标
        for (Unit target : marked) {
            PassiveUtils.hpUpdate(context, skillReport, owner, target, -dmgDecr / marked.size(), time, passiveState);
        }
    }

    @Override
    public PassiveType getType() {
        return PassiveType.ZAI_HUO_YANG_LING_ZS;
    }
}
