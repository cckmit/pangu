package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.UnitHpChangePassive;
import com.pangu.logic.module.battle.service.passive.param.BuffUpdateParam;
import com.pangu.logic.module.battle.service.passive.param.ZuiZhongShenPanParam;
import com.pangu.logic.module.battle.service.skill.effect.BuffUpdate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 最终审判:
 * 开启后获得6秒的狂暴(魔法免疫,并提升40%攻速),剑盾合一召唤出一把巨型的双手火焰大剑进行一次旋风斩击,对周围所有敌人造成165%物理伤害,100%概率造成昏迷效果1.5秒<br/>
 * 2级:暴怒期间,每恢复20%的最大生命值则延长1秒<br/>
 * 3级:伤害提升至185%<br/>
 * 4级:造成伤害的同时,并对敌人造成流血的效果,每秒损失50%伤害的生命值,持续3秒<br/>
 * <p/>
 * 该被动仅用于启动流血效果
 */
@Component
public class ZuiZhongShenPan implements AttackPassive, UnitHpChangePassive {
    @Autowired
    private BuffUpdate buffUpdate;

    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (damage >= 0) {
            return;
        }

        final ZuiZhongShenPanParam param = passiveState.getParam(ZuiZhongShenPanParam.class);

        //  指定tag的技能才能触发该被动
        if (!skillState.getTag().equals(param.getSkillTag())) {
            return;
        }

        //  将该技能造成的伤害按一定比例缓存，作为dot buff的循环伤害
        final double dmgFactor = param.getDmgFactor();
        if (dmgFactor <= 0) {
            return;
        }
        final long dotDmg = (long) (dmgFactor * damage);

        BuffFactory.addBuff(param.getDeBuffId(), owner, target, time, skillReport, dotDmg);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.ZUI_ZHONG_SHEN_PAN;
    }

    @Override
    public void hpChange(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> changeUnit) {
        final ZuiZhongShenPanParam param = passiveState.getParam(ZuiZhongShenPanParam.class);
        final double buffDelayCondition = param.getCondition();
        if (buffDelayCondition <= 0) {
            return;
        }
        if (!changeUnit.contains(owner)) {
            return;
        }

        final Addition addition = passiveState.getAddition(Addition.class, new Addition());
        //获取上轮hp变化后的值
        Long preHp = addition.preHp;
        final long hpMax = owner.getValue(UnitValue.HP_MAX);
        if (preHp == null) {
            if (hpMax == 0) {
                return;
            }
            preHp = hpMax;
        }

        //获取当前生命值并记录
        final long curHP = owner.getValue(UnitValue.HP);
        addition.preHp = curHP;

        //身上不存在指定tag的buff时清空累计回复
        final BuffState buffState = owner.getBuffStateByTag(param.getDelayBuffTag());
        if (buffState == null) {
            addition.remainder = 0;
            return;
        }

        //不对伤害做任何处理
        final long hpChange = curHP - preHp;
        if (hpChange <= 0) {
            return;
        }

        //将上轮的零头与本轮发生的回复相加
        final double remainder = addition.remainder;
        final double accRecoverHpPct = remainder + hpChange / 1.0 / hpMax;

        //累计效果足以触发多少次奖励
        final double dTimes = accRecoverHpPct / buffDelayCondition;
        final int lTimes = (int) dTimes;

        //缓存本轮零头
        addition.remainder = (dTimes - lTimes) * buffDelayCondition;

        if (lTimes < 1) {
            return;
        }

        //延长buff时间
        final BuffUpdateParam buffUpdateParam = new BuffUpdateParam();
        BeanUtils.copyProperties(param.getBuff(), buffUpdateParam);
        buffUpdateParam.setReset(false);
        final int delay = lTimes * buffUpdateParam.getDelay();
        buffUpdateParam.setDelay(delay);
        buffUpdate.doBuffUpdate(buffUpdateParam, owner, owner, damageReport, time);

        //延长魔免时间
        final UnitState unitState = param.getUnitState();
        owner.addState(unitState, owner.getStateValidTime()[unitState.ordinal()] + delay);
    }

    private static class Addition {
        private Long preHp;
        private double remainder;
    }
}
