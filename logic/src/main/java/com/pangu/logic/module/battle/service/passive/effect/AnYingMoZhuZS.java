package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.UnitRate;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackBeforePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillReleasePassive;
import com.pangu.logic.module.battle.service.passive.SkillSelectPassive;
import com.pangu.logic.module.battle.service.passive.param.AnYingMoZhuZSParam;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import com.pangu.logic.module.battle.service.skill.param.AnYingKuangBaoParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 暗影魔主·奥古斯丁专属装备
 * 1：每3次普攻后，下次普攻替换为弱化版的暗影狂暴，造成50%原有伤害
 * 10：造成60%原有伤害
 * 20：造成75%原有伤害
 * 30：每次成功施法会使后续弱化的暗影狂暴的伤害提高10%，最多叠加3次，与暗影狂暴效果分开独立计算
 *
 * @author Kubby
 */
@Component
public class AnYingMoZhuZS implements SkillSelectPassive, SkillReleasePassive, AttackBeforePassive {

    @Override
    public PassiveType getType() {
        return PassiveType.AN_YING_MO_ZHU_ZS;
    }

    @Override
    public SkillState skillSelect(PassiveState passiveState, SkillState skillState, Unit owner, int time) {

        AnYingMoZhuZSParam param = passiveState.getParam(AnYingMoZhuZSParam.class);

        AnYingMoZhuZSAddition addition = passiveState
                .getAddition(AnYingMoZhuZSAddition.class, new AnYingMoZhuZSAddition());

        addition.reset();

        if (skillState.getType() != SkillType.NORMAL) {
            return null;
        }

        if (addition.normalTime < param.getNormalTimes()) {
            return null;
        }

        List<SkillState> anYingKuangBaoSkills = owner.getActiveSkillsByTag(param.getSkillTag());
        if (anYingKuangBaoSkills.isEmpty()) {
            return null;
        }

        
        if (addition.skillState == null) {
            SkillState sourceSkill = anYingKuangBaoSkills.get(0);
            String skillId = sourceSkill.getId();
            addition.skillState = SkillFactory.initState(skillId);
            addition.skillState.setCd(param.getCd());
            for (EffectState effectState : addition.skillState.getEffectStates()) {
                if (effectState.getType() == EffectType.AN_YING_KUANG_BAO) {
                    AnYingKuangBaoParam copy = effectState.getParam(AnYingKuangBaoParam.class).copy();
                    copy.setFactor(copy.getFactor() * param.getWeakFactor());
                    effectState.setParamOverride(copy);
                }
            }
        }

        addition.use();

        return addition.skillState;
    }

    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time,
                             Context context, SkillReport skillReport) {
        if (owner != attacker) {
            return;
        }

        AnYingMoZhuZSAddition addition = passiveState
                .getAddition(AnYingMoZhuZSAddition.class, new AnYingMoZhuZSAddition());

        
        if (skillState.getType() == SkillType.NORMAL) {
            addition.normalTime++;
        }
    }

    @Override
    public void attackBefore(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time,
                             Context context, SkillReport skillReport) {
        AnYingMoZhuZSAddition addition = passiveState
                .getAddition(AnYingMoZhuZSAddition.class, new AnYingMoZhuZSAddition());

        if (addition.notInUse()) {
            return;
        }

        AnYingMoZhuZSParam param = passiveState.getParam(AnYingMoZhuZSParam.class);

        if (param.getDmgRate() > 0) {
            int continueTime = param.getDmgContinueTime();
            int prevValidTime = addition.validTime;
            addition.validTime = time + continueTime;

            if (prevValidTime > time) {
                int overlayLimit = param.getOverlayLimit();
                if (addition.overlayTime < overlayLimit) {
                    addition.overlayTime++;
                }
            } else {
                addition.overlayTime = 1;
            }

            double upRate = addition.overlayTime * param.getDmgRate();
            owner.increaseRate(UnitRate.HARM_P, upRate);
            owner.increaseRate(UnitRate.HARM_M, upRate);
        }
    }

    @Override
    public void attackEnd(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time,
                          Context context, SkillReport skillReport) {
        AnYingMoZhuZSAddition addition = passiveState
                .getAddition(AnYingMoZhuZSAddition.class, new AnYingMoZhuZSAddition());

        if (addition.notInUse()) {
            return;
        }

        AnYingMoZhuZSParam param = passiveState.getParam(AnYingMoZhuZSParam.class);

        if (param.getDmgRate() > 0) {
            double upRate = addition.overlayTime * param.getDmgRate();
            owner.increaseRate(UnitRate.HARM_P, -upRate);
            owner.increaseRate(UnitRate.HARM_M, -upRate);
        }
    }

    public class AnYingMoZhuZSAddition {

        /**
         * 是否正在使用替换的技能
         */
        boolean use;

        /**
         * 替换的技能
         */
        SkillState skillState;

        /**
         * 普攻累计次数
         */
        int normalTime;

        /**
         * 有效时间
         */
        int validTime;

        /**
         * 累加次数
         */
        int overlayTime;

        boolean notInUse() {
            return !use;
        }

        void reset() {
            use = false;
        }

        void use() {
            use = true;
            normalTime = 0;
        }

    }
}
