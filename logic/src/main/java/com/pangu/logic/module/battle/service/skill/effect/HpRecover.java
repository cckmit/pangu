package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.ctx.HpRecoverCtx;
import com.pangu.logic.module.battle.service.skill.param.HpRecoverParam;
import com.pangu.logic.module.common.resource.Formula;
import com.pangu.framework.resource.Storage;
import com.pangu.framework.resource.anno.Static;
import com.pangu.framework.utils.math.RandomUtils;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * 血量回复
 * 单位施加150%攻击力的恢复效果
 * 2级:该技能每次成功使用，则之后下一次该技能的效果提升25%，至多提升150%
 * 3级:治疗量提升至170%攻击力
 * 4级:冷却时间降低为10秒
 */
@Component
public class HpRecover implements SkillEffect {

    @Static
    private Storage<String, Formula> formulaStorage;

    //  是否暴击计算公式
    @Static("FIGHT:HP:RECOVER:ISCRIT")
    private Formula isCritFormula;
    //  回复值公式:暴击
    @Static("FIGHT:HP:RECOVER:CRIT")
    private Formula critFormula;
    //  回复值公式:法术
    @Static("FIGHT:HP:RECOVER:NORMAL:M")
    private Formula normalFormula;

    @Override
    public EffectType getType() {
        return EffectType.HP_RECOVER;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        HpRecoverParam param = state.getParam(HpRecoverParam.class);
        RecoverResult recoverResult = calcRecoverRes(owner, target, param);
        context.addValue(target, AlterType.HP, recoverResult.recover);
        skillReport.add(time, target.getId(), Hp.fromRecover(recoverResult.recover, recoverResult.crit, false));

        int timesLimit = param.getTimesLimit();
        if (timesLimit <= 0) {
            return;
        }
        double addRate = param.getAddRate();
        if (addRate <= 0) {
            return;
        }
        Integer curTimes = state.getAddition(Integer.class);
        if (curTimes == null) {
            curTimes = 0;
        }
        if (curTimes >= timesLimit) {
            return;
        }
        state.setParamOverride(
                new HpRecoverParam(param.getFactor() + addRate,
                        param.getFormulaIds(),
                        param.getTimesLimit(),
                        param.getAddRate(),
                        param.getPercent(),
                        param.getRate()));

        state.setAddition(curTimes + 1);
    }

    public RecoverResult calcRecoverRes(Unit owner, Unit target, HpRecoverParam param) {
        Formula critFormula = this.critFormula;
        Formula normalFormula = this.normalFormula;
        String[] formulaIds = param.getFormulaIds();
        RecoverResult recoverResult = new RecoverResult();
        if (formulaIds != null && formulaIds.length == 2) {
            String critId = formulaIds[0];
            critFormula = formulaStorage.get(critId, true);
            String normalId = formulaIds[1];
            normalFormula = formulaStorage.get(normalId, true);
        }

        HpRecoverCtx formulaCtx = new HpRecoverCtx(owner, target, param.getFactor(), param.getPercent(), param.getRate());
        double rate = (Double) isCritFormula.calculate(formulaCtx);
        recoverResult.crit = RandomUtils.isHit(rate);
        if (recoverResult.crit) {
            recoverResult.recover = ((Number) critFormula.calculate(formulaCtx)).longValue();
        } else {
            recoverResult.recover = ((Number) normalFormula.calculate(formulaCtx)).longValue();
        }


        if (recoverResult.recover < 0) {
            recoverResult.recover = 0;
        }
        return recoverResult;
    }

    @Getter
    public static class RecoverResult {
        private long recover;
        private boolean crit;

        public static RecoverResult of(long recover, boolean crit) {
            RecoverResult result = new RecoverResult();
            result.recover = recover;
            result.crit = crit;
            return result;
        }
    }
}
