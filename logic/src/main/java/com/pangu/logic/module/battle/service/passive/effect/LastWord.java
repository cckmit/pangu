package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.Battle;
import com.pangu.logic.module.battle.service.action.SkillEffectAction;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.OwnerDiePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.LastWordParam;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import com.pangu.logic.module.battle.service.skill.effect.HpMagicDamage;
import com.pangu.logic.module.battle.service.skill.effect.HpPhysicsDamage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 持有者死亡时执行指定行为
 */
@Component
public class LastWord implements OwnerDiePassive {
    @Autowired
    private HpPhysicsDamage physicsDamage;
    @Autowired
    private HpMagicDamage magicDamage;

    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attack, ITimedDamageReport timedDamageReport, int time, Context context) {
        // 计算cd
        passiveState.addCD(time);

        final LastWordParam param = passiveState.getParam(LastWordParam.class);
        switch (param.getWordType()) {
            case BUFF_CAST: {
                final Map<String, String> buffMap = param.getBuffMap();
                if (CollectionUtils.isEmpty(buffMap)) return;
                final List<Unit> select = new ArrayList<>();
                for (Map.Entry<String, String> entry : buffMap.entrySet()) {
                    if (StringUtils.isEmpty(entry.getValue())) {
                        select.add(attack);
                    } else {
                        select.addAll(TargetSelector.select(owner, entry.getValue(), time));
                    }
                    select.forEach(unit -> BuffFactory.addBuff(entry.getKey(), owner, unit, time, timedDamageReport, null));
                }
                break;
            }
            case REVIVE: {
                //  我方存活角色只余自身时是否触发复活
                if (!param.isRevivableWhenAced() && owner.getFriend().getCurrent().isEmpty()) {
                    return;
                }

                //  受到不可复活的即死效果时无法复活
                final boolean success = owner.revive(time);
                if (!success) {
                    return;
                }

                final long value = owner.getValue(UnitValue.HP) + context.getHpChange(owner);
                // 保留1点血
                context.addPassiveValue(owner, AlterType.HP, -value + 1);
//                timedDamageReport.add(time, owner.getId(), Hp.of(-value + 1));
                final SkillState skillState = SkillFactory.initState(param.getSkillId());

                //复活一般需要延时播放动画，此期间不可被选中，且无敌
                final Optional<EffectState> maxDelayEffect = skillState.getEffectStates().stream()
                        .max(Comparator.comparingInt(EffectState::getDelay));
                int maxDelay = 0;
                if (maxDelayEffect.isPresent()) {
                    maxDelay = maxDelayEffect.get().getDelay();
                }
                final int reviveDelay = skillState.getSingTime() + skillState.getFirstTimeDelay() + maxDelay;
                final int reviveTime = reviveDelay + time;
                owner.addState(UnitState.UNVISUAL, reviveTime);
                owner.addState(UnitState.WU_DI, reviveTime);
                owner.addState(UnitState.BA_TI, reviveTime);
                SkillFactory.updateNextExecuteSkill(time, owner, skillState);
                break;
            }
            case WORLD_EFFECT: {
                final String skillId = param.getSkillId();
                final SkillState skillState = SkillFactory.initState(skillId);
                final Unit faceTarget = owner.getTarget();
                final SkillReport skillReport = SkillReport.sing(time, owner.getId(), skillId, 0, faceTarget == null ? null : faceTarget.getId());
                final SkillEffectAction worldAction = new SkillEffectAction(time, owner, skillState, skillReport);
                final Battle battle = owner.getBattle();
                battle.addReport(skillReport);
                battle.addWorldAction(worldAction);
                break;
            }
        }
    }

    @Override
    public PassiveType getType() {
        return PassiveType.LAST_WORD;
    }
}
