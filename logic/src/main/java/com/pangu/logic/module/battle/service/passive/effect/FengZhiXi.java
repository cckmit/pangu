package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.resource.FightSkillSetting;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackBeforePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillSelectPassive;
import com.pangu.logic.module.battle.service.passive.param.FengZhiXiParam;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import com.pangu.framework.resource.Storage;
import com.pangu.framework.resource.anno.Static;
import com.pangu.framework.utils.math.RandomUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 斩风之息·武技能：风之息
 * 1级：每次普攻暴击时都会获得一层风之息效果，在积攒3层风之息效果后,下一次普攻会以当前目标为方向释放一道能够击飞敌人的刀光,对当前目标造成140%攻击的物理伤害
 * 2级：攻击伤害提升至180%
 * 3级：刀光会向前移动一段距离,对直线上的其他敌人造成相同伤害并将其击飞
 * 4级：攻击伤害提升至220%
 * @author Kubby
 */
@Component
public class FengZhiXi implements SkillSelectPassive, AttackBeforePassive {

    @Static
    private Storage<String, FightSkillSetting> skillStorage;

    @Override
    public PassiveType getType() {
        return PassiveType.FENG_ZHI_XI;
    }

    @Override
    public SkillState skillSelect(PassiveState passiveState, SkillState skillState, Unit owner, int time) {
        if (skillState.getType() != SkillType.NORMAL) {
            return null;
        }

        FengZhiXiParam param = passiveState.getParam(FengZhiXiParam.class);
        List<BuffState> buffStates = owner.getBuffBySettingId(param.getBuffId());
        int overlay = buffStates.size();

        if (overlay < param.getOverlayCount()) {
            return null;
        }

        /* 重置风之息BUFF */
        for (BuffState buffState : buffStates) {
            BuffFactory.removeBuffState(buffState, owner, time);
        }

        FengZhiXiAddition addition = passiveState.getAddition(FengZhiXiAddition.class, new FengZhiXiAddition());
        if (addition.skill == null) {
            addition.skill = SkillFactory.initState(param.getSkillId());
        }

        return addition.skill;
    }

    @Override
    public void attackBefore(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time,
                             Context context, SkillReport skillReport) {
    }

    @Override
    public void attackEnd(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time,
                          Context context, SkillReport skillReport) {
        FightSkillSetting skillSetting = skillStorage.get(skillReport.getSkillId(), true);
        if (skillSetting.getType() != SkillType.NORMAL) {
            return;
        }
        /* 触发概率 */
        FengZhiXiParam param = passiveState.getParam(FengZhiXiParam.class);
        if (!RandomUtils.isHit(param.getRate())) {
            return;
        }
        /* 叠加一层风之息BUFF */
        BuffFactory.addBuff(param.getBuffId(), owner, owner, time, skillReport, null);
    }

    private static class FengZhiXiAddition {

        SkillState skill;

    }

}
