package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.*;
import com.pangu.logic.module.battle.service.passive.param.BuffUpdateParam;
import com.pangu.logic.module.battle.service.passive.param.YueZhiShenPanParam;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import com.pangu.logic.module.battle.service.skill.effect.BuffUpdate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 月之审判
 * 第3次普通攻击会强化成审判攻击,伤害提升40%,必定给目标附带一个审判印记,一个敌人最多附加5层
 * 2级:攻击暴击时必定附加3层
 * 3级:友方英雄被击杀时,会给击杀者5层印记
 * 4级:伤害额外提升80%
 */
@Component
public class YueZhiShenPan implements SkillSelectPassive, SkillReleasePassive, UnitDiePassive, AttackPassive {
    @Autowired
    private BuffUpdate buffUpdate;

    @Override
    public PassiveType getType() {
        return PassiveType.YUE_ZHI_SHEN_PAN;
    }

    //每X次普攻替换强化普攻
    @Override
    public SkillState skillSelect(PassiveState passiveState, SkillState skillState, Unit owner, int time) {
        if (skillState.getType() != SkillType.NORMAL) {
            return null;
        }
        final Addition addition = getAddition(passiveState);
        final YueZhiShenPanParam param = passiveState.getParam(YueZhiShenPanParam.class);
        if (addition.normalAtkCount < param.getTriggerCount() - 1) {
            return null;
        }
        addition.normalAtkCount = 0;
        return SkillFactory.initState(param.getSkillId());
    }

    //释放非强化普攻时，统计计数
    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time, Context context, SkillReport skillReport) {
        if (skillState.getType() != SkillType.NORMAL || !skillState.getTag().equals("cang_bai_zhi_pu_normal")) {
            return;
        }
        final Addition addition = getAddition(passiveState);
        addition.normalAtkCount++;
    }

    //友军被击杀时，对击杀者添加印记
    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> dieUnits) {
        if (!attacker.canSelect(time)) {
            return;
        }
        if (dieUnits.stream().noneMatch(dieUnit -> dieUnit.getFriend() == owner.getFriend())) {
            return;
        }
        final YueZhiShenPanParam param = passiveState.getParam(YueZhiShenPanParam.class);
        if (param.getAddCountWhenFriendDie() <= 0) {
            return;
        }
        final BuffUpdateParam buffUpdateParam = new BuffUpdateParam();
        BeanUtils.copyProperties(param.getBuff(), buffUpdateParam);
        buffUpdateParam.setAddition(param.getAddCountWhenFriendDie());
        buffUpdate.doBuffUpdate(buffUpdateParam, owner, attacker, damageReport, time);
    }

    //强化普攻对目标添加印记
    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (skillState.getType() != SkillType.NORMAL || !skillState.getTag().equals("yuan_zhi_shen_pan_normal")) {
            return;
        }
        final YueZhiShenPanParam param = passiveState.getParam(YueZhiShenPanParam.class);

        //造成伤害
        final long dmgChange = (long) (param.getDmgUpRate() * damage);
        PassiveUtils.hpUpdate(context, skillReport, target, dmgChange, time);

        //添加印记
        final BuffUpdateParam buffUpdateParam = new BuffUpdateParam();
        BeanUtils.copyProperties(param.getBuff(), buffUpdateParam);
        final int addCountWhenCrit = param.getAddCountWhenCrit();
        if (context.isCrit(target) && addCountWhenCrit > 0) {
            buffUpdateParam.setAddition(addCountWhenCrit);
        } else {
            buffUpdateParam.setAddition(param.getAddCount());
        }
        buffUpdate.doBuffUpdate(buffUpdateParam, owner, target, skillReport, time);
    }

    private Addition getAddition(PassiveState passiveState) {
        Addition addition = passiveState.getAddition(Addition.class);
        if (addition == null) {
            addition = new Addition();
            passiveState.setAddition(addition);
        }
        return addition;
    }


    private static class Addition {
        private int normalAtkCount;
    }
}
