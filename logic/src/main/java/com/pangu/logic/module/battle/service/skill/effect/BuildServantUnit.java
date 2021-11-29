package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.UnitInfo;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.SummonUnits;
import com.pangu.logic.module.battle.service.core.*;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.BuildServantUnitParam;
import com.pangu.logic.module.battle.service.skill.param.SummonSkillParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * 将技能添加到施法柱上，若施法柱不存在，则构建施法柱
 */
@Component
public class BuildServantUnit implements SkillEffect {
    @Autowired
    private SummonSkill summonSkill;

    @Override
    public EffectType getType() {
        return EffectType.BUILD_SERVANT_UNIT;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final BuildServantUnitParam param = state.getParam(BuildServantUnitParam.class);
        final SummonSkillParam buildParam = initBuildParam(param);

        final String masterSelectId = buildParam.getTargetId();

        //  定位施法柱单元的属性参考单元
        Unit masterTarget;
        if (masterSelectId.equals("SELF")) {
            masterTarget = owner;
        } else {
            final List<Unit> masterCandidates = TargetSelector.select(owner, masterSelectId, time);
            if (CollectionUtils.isEmpty(masterCandidates)) {
                masterTarget = owner;
            } else {
                masterTarget = masterCandidates.get(0);
            }
        }

        //  获取已存在的施法柱
        Unit servantUnit;
        Fighter inFighter;
        if (param.isBuildForEnemy()) {
            inFighter = owner.getEnemy();
        } else {
            inFighter = owner.getFriend();
        }
        servantUnit = inFighter.getServantByMaster(masterTarget);

        //  若不存在则构建
        if (servantUnit == null) {
            servantUnit = summonSkill.summonUnit(owner, buildParam, masterTarget, 0, time);
            context.addSummon(Collections.singletonList(servantUnit));
            inFighter.registerServant(masterTarget, servantUnit);

            final SummonUnits summonVal = new SummonUnits(Collections.singletonList(UnitInfo.valueOf(servantUnit)));
            summonVal.setUnitType(buildParam.getUnitType());
            skillReport.add(time, owner.getId(), summonVal);
        }

        //  为施法柱添加初始化技能
        servantUnit.addSkill(param.getServantSkill());
    }

    private SummonSkillParam initBuildParam(BuildServantUnitParam param) {
        final SummonSkillParam buildParam = new SummonSkillParam();
        buildParam.setBaseId(param.getBaseId());
        buildParam.setJoinFighter(false);
        buildParam.setSummon(false);
        buildParam.setRate(1);
        buildParam.setUnitType(2);
        return buildParam;
    }
}
