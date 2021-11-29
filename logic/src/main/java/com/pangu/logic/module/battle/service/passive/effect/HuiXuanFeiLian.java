package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackEndPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.HuiXuanFeiLianParam;
import com.pangu.logic.module.battle.service.skill.effect.MpChange;
import com.pangu.framework.utils.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * 回旋飞镰
 * 投掷回旋飞镰,造成两次130%攻击力的范围伤害飞镰返回时,有70%几率接住飞镰,如果成功接住飞镰,一段时间内攻击速度得到提升10%该技能10秒触发一次
 * 2级:伤害提升至145%攻击力,接住飞镰可以回复50点能量
 * 3级:伤害提升至160%攻击力
 * 4级:攻击速度提升至20%
 */
@Component
public class HuiXuanFeiLian implements AttackEndPassive {
    @Autowired
    private MpChange mpChange;

    @Override
    public void attackEnd(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (skillState.getType() != SkillType.SKILL || context.getLoopTimes() < 2) return;
        final HuiXuanFeiLianParam param = passiveState.getParam(HuiXuanFeiLianParam.class);
        if (!RandomUtils.isHit(param.getTriggerRate())) return;
        //添加buff
        BuffFactory.addBuff(param.getBuffId(), owner, owner, time, skillReport, null);
        //回复mp
        final int mp = param.getMp();
        //小于0时不应做任何处理
        if (mp <= 0) return;
        final EffectState effectState = new EffectState(null, 0);
        effectState.setParamOverride(mp);
        mpChange.execute(effectState, owner, owner, skillReport, time, skillState, context);
        effectState.setParamOverride(null);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.HUI_XUAN_FEI_LIAN;
    }
}
