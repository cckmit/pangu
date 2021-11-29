package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.*;
import com.pangu.logic.module.battle.service.passive.param.ZhanXingMoOuZSParam;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 1：星球每次因技能产生移动之后，的下次攻击会造成150%伤害，并溅射目标周围所有敌人，并减少所有收到伤害的敌人100能量
 * 10：星球每次因技能产生移动之后，自身额外回能80
 * 20：星球每次因技能产生移动之后，自身额外回能130
 * 30：若下次攻击击杀敌人，则回复自身300能量
 * <p>
 * 此被动较为复杂，注释说不清楚，如有疑问请联系
 * @author cicala
 */
@Component
public class ZhanXingMoOuZS implements UnitDiePassive, AttackPassive, SkillReleasePassive, SkillSelectPassive {
    @Override
    public PassiveType getType() {
        return PassiveType.ZHAN_XING_MO_OU_ZS;
    }

    //命中目标增伤+扣能量
    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        final Addition.State normalAtkState = getAddition(passiveState).normalAtkState;
        if (normalAtkState != Addition.State.CONSUMING) {
            return;
        }
        final ZhanXingMoOuZSParam param = passiveState.getParam(ZhanXingMoOuZSParam.class);
        final long dmgChange = (long) ((param.getDmgRate() - 1) * damage);
        PassiveUtils.hpUpdate(context, skillReport, target, dmgChange, time);
        PassiveUtils.mpUpdate(context, skillReport, owner, target, -param.getMpCut(), time, passiveState);
    }

    //本被动的状态机
    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time, Context context, SkillReport skillReport) {
        final Addition addition = getAddition(passiveState);
        final SkillType skillType = skillState.getType();
        //当技能使球发生位移（大招和技能会使球发生位移），会强化下次普攻，并回能
        final boolean consumableTriggered = skillType == SkillType.SPACE || skillType == SkillType.SKILL;
        //当技能类型为普攻，会使当次普攻得到的强化
        final boolean consumingTriggered = skillType == SkillType.NORMAL;

        switch (addition.normalAtkState) {
            case CONSUMING: {
                if (consumableTriggered) {
                    addition.normalAtkState = Addition.State.CONSUMABLE;
                    PassiveUtils.mpUpdate(context, skillReport, owner, owner, passiveState.getParam(ZhanXingMoOuZSParam.class).getMpRecover(), time, passiveState);
                } else {
                    addition.normalAtkState = Addition.State.CONSUMED;
                }
                break;
            }
            case CONSUMABLE: {
                if (consumingTriggered) {
                    addition.normalAtkState = Addition.State.CONSUMING;
                }
                break;
            }
            case CONSUMED: {
                if (consumableTriggered) {
                    addition.normalAtkState = Addition.State.CONSUMABLE;
                    PassiveUtils.mpUpdate(context, skillReport, owner, owner, passiveState.getParam(ZhanXingMoOuZSParam.class).getMpRecover(), time, passiveState);
                }
                break;
            }
        }
        switch (addition.killMpRecoverState) {
            case CONSUMING: {
                if (consumableTriggered) {
                    addition.killMpRecoverState = Addition.State.CONSUMABLE;
                } else {
                    addition.killMpRecoverState = Addition.State.CONSUMED;
                }
                break;
            }
            case CONSUMABLE: {
                addition.killMpRecoverState = Addition.State.CONSUMING;
                break;
            }
            case CONSUMED: {
                if (consumableTriggered) {
                    addition.killMpRecoverState = Addition.State.CONSUMABLE;
                }
                break;
            }
        }
    }

    //标记替换普攻
    @Override
    public SkillState skillSelect(PassiveState passiveState, SkillState skillState, Unit owner, int time) {
        if (skillState.getType() != SkillType.NORMAL) {
            return null;
        }
        final Addition.State normalAtkState = getAddition(passiveState).normalAtkState;
        if (normalAtkState != Addition.State.CONSUMABLE) {
            return null;
        }
        return SkillFactory.initState(passiveState.getParam(ZhanXingMoOuZSParam.class).getReplaceSkill());
    }

    //击杀回能
    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attacker, int time, Context context, ITimedDamageReport damageReport, Set<Unit> dieUnits) {
        if (attacker != owner) {
            return;
        }
        final Addition addition = getAddition(passiveState);
        //必须为使用技能后的下一次攻击击杀，才回复能量
        if (addition.killMpRecoverState != Addition.State.CONSUMING) {
            return;
        }
        final ZhanXingMoOuZSParam param = passiveState.getParam(ZhanXingMoOuZSParam.class);
        final int mpChange = (param.getMpRecoverWhenKill() - owner.getBattle().getConfig().getKillAddMp()) * dieUnits.size();
        PassiveUtils.mpUpdate(context, damageReport, owner, owner, mpChange, time, passiveState);
    }

    public Addition getAddition(PassiveState passiveState) {
        Addition addition = passiveState.getAddition(Addition.class);
        if (addition == null) {
            addition = new Addition();
            passiveState.setAddition(addition);
        }
        return addition;
    }

    @Getter
    @Setter
    public static class Addition {
        private Addition.State normalAtkState = Addition.State.CONSUMED;
        private Addition.State killMpRecoverState = Addition.State.CONSUMED;

        public enum State {
            CONSUMABLE, CONSUMING, CONSUMED
        }
    }
}
