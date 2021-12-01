package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.ReviveValue;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.param.BuffUpdateParam;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.HpRecoverParam;
import com.pangu.logic.module.battle.service.skill.param.YuanXingDeHuHuanParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 唤星女神·维纳斯技能：远星的呼唤
 * 1级：回复己方全体280%法攻的血量，当有3个神灵晶石状态时,可以复活队友,一次战斗可复活1次,拥有光明大祭司法攻600%的血量,没有死亡队友的情况下不消耗神灵晶石状态
 * 2级：技能治疗提升至300%
 * 3级：一次战斗可复活2次
 * 4级：技能治疗提升至320%,复活效果提升至700%
 *
 * @author Kubby
 */
@Component
public class YuanXingDeHuHuan implements SkillEffect {

    @Autowired
    private HpRecover hpRecover;

    @Autowired
    private BuffUpdate buffUpdate;

    @Override
    public EffectType getType() {
        return EffectType.YUAN_XING_DE_HU_HUAN;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time,
                        SkillState skillState, Context context) {
        YuanXingDeHuHuanParam param = state.getParam(YuanXingDeHuHuanParam.class);

        int reviveTimes = state.getAddition(int.class, 0);

        if (reviveTimes >= param.getReviveLimit()) {
            return;
        }

        List<Unit> candidates = new ArrayList<>();
        for (Unit unit : owner.getFriend().getDieUnit()) {
            if (unit.isSummon()) {
                continue;
            }
            candidates.add(unit);
        }

        if (candidates.isEmpty()) {
            return;
        }

        List<BuffState> stoneBuffs = owner.getBuffByClassify(param.getStoneBuffId());
        if (stoneBuffs.size() < param.getStoneRequire()) {
            return;
        }

        int count = 0;
        for (Unit candidate : candidates) {
            if (count > param.getReviveCount()) {
                continue;
            }
            count++;
            candidate.setRevivable(true);
            candidate.revive(time);
            skillReport.add(time, candidate.getId(), new ReviveValue());

            EffectState cureState = new EffectState(null, 0);
            cureState.setParamOverride(new HpRecoverParam(param.getFactor()));

            hpRecover.execute(cureState, owner, candidate, skillReport, time, skillState, context);


            final int zsMpAdd = param.getZsMpAdd();
            if (zsMpAdd > 0) {
                context.addValue(candidate, AlterType.MP, zsMpAdd);
                skillReport.add(time, candidate.getId(), new UnitValues(AlterType.MP, zsMpAdd));
            }

            final int zsSelfMpAdd = param.getZsSelfMpAdd();
            if (zsSelfMpAdd > 0) {
                context.addValue(owner, AlterType.MP, zsSelfMpAdd);
                skillReport.add(time, owner.getId(), new UnitValues(AlterType.MP, zsSelfMpAdd));
            }

            if (!StringUtils.isBlank(param.getZsBuffId())) {
                BuffFactory.addBuff(param.getZsBuffId(), owner, candidate, time, skillReport, null);
            }
        }

        for (int i = 0; i < param.getStoneRequire(); i++) {
            BuffState buffState = stoneBuffs.get(i);
            BuffFactory.removeBuffState(buffState, owner, time);
        }
        //消耗石头用于表现
        final EffectState stoneCounterChangeState = new EffectState(null, 0);
        final BuffUpdateParam stoneCounter = param.getStoneCounter();
        stoneCounter.setAddition(-param.getStoneRequire());
        stoneCounterChangeState.setParamOverride(stoneCounter);
        buffUpdate.execute(stoneCounterChangeState, owner, owner, skillReport, time, skillState, context);
        state.setAddition(reviveTimes + 1);
    }
}
