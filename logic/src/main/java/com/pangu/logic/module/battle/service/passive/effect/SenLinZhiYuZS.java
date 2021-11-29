package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.UnitRate;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.SenLinZhiYuZSParam;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.skill.effect.HpRecover;
import com.pangu.logic.module.battle.service.skill.param.HpRecoverParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 森林絮语·玛法达专属装备
 * 1：普攻时治疗虚弱的友军，恢复自己攻击力60%
 * 10：普攻暴击时，恢复效果提升75%
 * 20：普攻时治疗做虚弱的友军，恢复自己攻击力90%
 * 30：普攻时治疗做虚弱的友军，恢复自己攻击力120%
 * @author Kubby
 */
@Component
public class SenLinZhiYuZS implements AttackPassive {

    @Autowired
    private HpRecover hpRecover;

    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context,
                       SkillState skillState, SkillReport skillReport) {

        if (skillState.getType() != SkillType.NORMAL) {
            return;
        }

        SenLinZhiYuZSParam param = passiveState.getParam(SenLinZhiYuZSParam.class);

        EffectState cureState = new EffectState(null, 0);
        cureState.setParamOverride(new HpRecoverParam(param.getFactor()));

        boolean cureUp = false;
        if (param.getCritUpCureRate() > 0 && context.isCrit(owner)) {
            /* 添加临时属性 */
            owner.increaseRate(UnitRate.CURE, param.getCritUpCureRate());
            cureUp = true;
        }

        List<Unit> cureTargets = TargetSelector.select(owner, param.getSelectId(), time);

        for (Unit cureTarget : cureTargets) {
            hpRecover.execute(cureState, owner, cureTarget, skillReport, time, skillState, context);
        }

        if (cureUp) {
            /* 移除临时属性 */
            owner.increaseRate(UnitRate.CURE, -param.getCritUpCureRate());
        }
    }

    @Override
    public PassiveType getType() {
        return PassiveType.SEN_LIN_ZHI_YU_ZS;
    }
}
