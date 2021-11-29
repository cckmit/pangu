package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.DispelType;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillReleasePassive;
import com.pangu.logic.module.battle.service.passive.param.ManHaiZhuJiZSParam;
import com.pangu.logic.module.battle.service.skill.effect.BuffDispel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * 蛮海主祭·普鲁特专属装备
 * 1：每次释放大招时候，使能量超过50%的队友获得一次海之洗涤
 * 10：海之洗涤每清除一个DEBUFF，则恢复50点能量，最多250
 * 20：海之洗涤每清除一个DEBUFF，则恢复100点能量，最多400
 * 30：海之洗涤每清除一个DEBUFF，则恢复150点能量，最多550
 *
 * @author Kubby
 */
@Component
public class ManHaiZhuJiZS implements SkillReleasePassive {

    @Autowired
    private BuffDispel buffDispel;

    @Override
    public PassiveType getType() {
        return PassiveType.MAN_HAI_ZHU_JI_ZS;
    }

    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time,
                             Context context, SkillReport skillReport) {
        if (owner != attacker) {
            return;
        }
        if (skillState.getType() != SkillType.SPACE) {
            return;
        }

        ManHaiZhuJiZSParam param = passiveState.getParam(ManHaiZhuJiZSParam.class);

        List<Unit> targets = new LinkedList<>();

        /* 找出符合的目标 */
        int prevDebuffCount = 0;
        for (Unit unit : owner.getFriend().getCurrent()) {
            if (unit.getMpPct() >= param.getMpRequirePct()) {
                targets.add(unit);
                final List<BuffState> harmfulBuffs = unit.getBuffByDispel(DispelType.HARMFUL);
                if (CollectionUtils.isEmpty(harmfulBuffs)) {
                    continue;
                }
                prevDebuffCount += harmfulBuffs.size();
            }
        }

        EffectState effectState = new EffectState(null, 0);
        effectState.setParamOverride(DispelType.HARMFUL);

        /* 释放海之洗涤 */
        int currDebuffCount = 0;
        for (Unit target : targets) {
            buffDispel.execute(effectState, owner, target, skillReport, time, null, context);
            final List<BuffState> harmfulBuffs = target.getBuffByDispel(DispelType.HARMFUL);
            if (CollectionUtils.isEmpty(harmfulBuffs)) {
                continue;
            }
            prevDebuffCount += harmfulBuffs.size();
        }

        int dispelCount = prevDebuffCount - currDebuffCount;

        /* 恢复能量 */
        if (dispelCount > 0 && param.getMpCureValue() > 0) {
            long expectMpCureValue = dispelCount * param.getMpCureValue();
            long finalMpCureValue = Math.min(expectMpCureValue, param.getMpCureLimit());
            context.addPassiveValue(owner, AlterType.MP, finalMpCureValue);
            skillReport.add(time, owner.getId(),
                    PassiveValue.single(passiveState.getId(), owner.getId(), new UnitValues(AlterType.MP, finalMpCureValue)));
        }
    }
}
