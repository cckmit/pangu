package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackBeforePassive;
import com.pangu.logic.module.battle.service.passive.OwnerDiePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.FengKuangBoShiZSParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 疯狂博士·威尔金斯专属装备
 * 1：释放4个电磁炸弹之后，普攻伤害+10%
 * 10：释放8个电磁炸弹之后，普攻伤害额外+5%
 * 20：释放12个电磁炸弹之后，普攻伤害额外+10%
 * 30：释放15个电磁炸弹之后，普攻变为范围伤害
 * @author Kubby
 */
@Component
public class FengKuangBoShiZS implements AttackBeforePassive, OwnerDiePassive {

    @Override
    public PassiveType getType() {
        return PassiveType.FENG_KUANG_BO_SHI_ZS;
    }

    @Override
    public void attackBefore(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time,
                             Context context, SkillReport skillReport) {
    }

    @Override
    public void attackEnd(PassiveState passiveState, EffectState effectState, Unit owner, Unit target, int time,
                          Context context, SkillReport skillReport) {
        if (effectState.getType() != EffectType.DIAN_CI_ZHA_DAN) {
            return;
        }
        FengKuangBoShiZSParam param = passiveState.getParam(FengKuangBoShiZSParam.class);
        FengKuangBoShiZSAddition addition = passiveState
                .getAddition(FengKuangBoShiZSAddition.class, new FengKuangBoShiZSAddition());

        addition.times++;


        String buffId = getBuffId(param, addition.times);
        if (!StringUtils.isBlank(buffId)) {

            if (addition.buffState != null && !addition.buffState.getId().equals(buffId)) {
                BuffFactory.removeBuffState(addition.buffState, owner, time);
                addition.buffState = null;
            }

            if (addition.buffState == null) {
                addition.buffState = BuffFactory.addBuff(buffId, owner, owner, time, skillReport, null);
            }
        }

        if (!addition.effectStates.isEmpty()) {
            return;
        }


        if (param.getSelectTimes() > 0 && addition.times >= param.getSelectTimes()) {
            List<EffectState> effectStates = new LinkedList<>();
            for (SkillState skillState : owner.getActiveSkills()) {
                if (skillState.getType() != SkillType.NORMAL) {
                    continue;
                }
                for (EffectState es : skillState.getEffectStates()) {
                    if (es.getType() == EffectType.HP_P_DAMAGE) {
                        es.setTargetOverride(param.getSelectId());
                        effectStates.add(es);
                    }
                }
            }
            addition.effectStates = effectStates;
        }
    }

    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attack, ITimedDamageReport timedDamageReport, int time,
                    Context context) {
        FengKuangBoShiZSAddition addition = passiveState
                .getAddition(FengKuangBoShiZSAddition.class, new FengKuangBoShiZSAddition());

        addition.times = 0;
        if (addition.buffState != null) {
            BuffFactory.removeBuffState(addition.buffState, owner, time);
        }
        if (!addition.effectStates.isEmpty()) {
            for (EffectState effectState : addition.effectStates) {
                effectState.setTargetOverride(null);
            }
            addition.effectStates = Collections.emptyList();
        }
    }

    private String getBuffId(FengKuangBoShiZSParam param, int times) {
        String buffId = null;
        for (int i : param.getBuffIds().keySet()) {
            if (times < i) {
                break;
            }
            buffId = param.getBuffIds().get(i);
        }
        return buffId;
    }

    public class FengKuangBoShiZSAddition {

        int times;

        BuffState buffState;

        List<EffectState> effectStates = Collections.emptyList();

    }

}
