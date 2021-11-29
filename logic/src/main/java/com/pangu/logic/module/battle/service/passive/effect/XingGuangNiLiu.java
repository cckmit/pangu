package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.XingGuangNiLiuParam;
import com.pangu.logic.module.battle.service.skill.effect.MpChange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 星光逆流
 * 指挥占星球飞向敌方人多的区域,释放一股冲击波,对周围敌军造成135%的魔法伤害并在短暂延迟后将他们拉向占星球
 * 2级:伤害提升至180%
 * 3级:造成伤害后降低受伤敌人20%的攻击速度,持续4秒
 * 4级:每命中一个目标,则回复自己50点能量
 *
 * 此【被动】效果仅【添加debuff，回复能量】
 * 其余效果由相关【主动】效果完成
 * {@link com.pangu.logic.module.battle.service.skill.effect.XingGuangNiLiu}
 */
@Component("PASSIVE:XingGuangNiLiu")
public class XingGuangNiLiu implements AttackPassive {
    @Autowired
    private MpChange mpChange;

    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (skillState.getType() != SkillType.SPACE || damage >= 0) return;
        final XingGuangNiLiuParam param = passiveState.getParam(XingGuangNiLiuParam.class);
        //添加buff
        BuffFactory.addBuff(param.getBuffId(), owner, target, time, skillReport, null);
        //回复能量
        final EffectState effectState = new EffectState(null, 0);
        effectState.setParamOverride(param.getMp());
        mpChange.execute(effectState, owner, target, skillReport, time, skillState, context);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.XING_GUANG_NI_LIU;
    }
}
