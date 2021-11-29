package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillReleasePassive;
import com.pangu.logic.module.battle.service.passive.param.HuDieXianZiZSParam;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 蝴蝶仙子·莉亚娜专属装备
 * 1：迷踪之碟使用后，附近敌人攻击力-8%
 * 10：迷踪之碟使用后，附近敌人攻击力-11%
 * 20：迷踪之碟使用后，附近敌人攻击力-15%
 * 30：迷踪之碟使用后，附近敌人攻击力-20%
 * @author Kubby
 */
@Component
public class HuDieXianZiZS implements SkillReleasePassive {

    @Override
    public PassiveType getType() {
        return PassiveType.HU_DIE_XIAN_ZI_ZS;
    }

    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time,
                             Context context, SkillReport skillReport) {
        HuDieXianZiZSParam param = passiveState.getParam(HuDieXianZiZSParam.class);
        if (!skillState.getTag().equals(param.getSkillTag())) {
            return;
        }
        List<Unit> units = TargetSelector.select(owner, param.getSelectId(), time);
        for (Unit unit : units) {
            for (String buffId : param.getBuffIds()) {
                BuffFactory.addBuff(buffId, owner, unit, time, skillReport, null);
            }
        }
    }
}
