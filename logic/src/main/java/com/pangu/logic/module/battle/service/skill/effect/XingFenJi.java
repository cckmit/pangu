package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.MpFrom;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Mp;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.XingFenJiParam;
import org.springframework.stereotype.Component;

/**
 * 兴奋剂(3级)
 * 在战斗中，角色会始终跟随战斗力最高的存活队友移动，并使自己受到的伤害下降25%。使用必杀技时，
 * 使正在跟随的队友能量恢复至上限。队友恢复能量越多，则自已消耗能量越多，但至少消耗200点能量。
 * 2级:使用时还将使该队友在4秒内提升40%攻击力
 * 3级:使用时还将使该队友在4秒内提升60%攻击力
 * 4级：使自己受到的伤害下降40%
 */
@Component
public class XingFenJi implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.XING_FEN_JI;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        XingFenJiParam param = state.getParam(XingFenJiParam.class);

        //计算实际需要转移的MP
        long targetMp = target.getValue(UnitValue.MP);
        long mpMax = target.getValue(UnitValue.MP_MAX);
        int minCostMp = param.getMinCostMp();
        if (minCostMp < 0) {
            minCostMp = 200;
        }
        //目标需要回复的MP
        long diff = mpMax - targetMp;
        //当目标需要回复的MP未达到最小回复值时，取最小回复值
        long actualMpCost = Math.max(minCostMp, diff);
        //当施法者MP存量不足以回复目标所需回复量时，取施法者实际MP存量
        actualMpCost = Math.min(actualMpCost, owner.getValue(UnitValue.MP));

        //施法者将自身MP转移给目标
        context.addValue(owner, AlterType.MP, -actualMpCost);
        skillReport.add(time, owner.getId(), new Mp(-actualMpCost, MpFrom.SKILL));

        context.addValue(target, AlterType.MP, actualMpCost);
        skillReport.add(time, target.getId(), new Mp(diff, MpFrom.NORMAL));

        //添加buff
        String buffId = param.getBuff();
        BuffFactory.addBuff(buffId, owner, target, time, skillReport, null);
    }
}
