package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.MuShiFollowPassiveParam;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.NiaoZuiYiShengZSParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 鸟嘴医生·查尔斯专属装备（技能优先级要最低）
 * 1：增加跟随队友的攻击力和防御力，数值为自身8%
 * 10：增加跟随队友的攻击力和防御力，数值为自身16%
 * 20：增加跟随队友的攻击力和防御力，数值为自身30%
 * 30：增加跟随队友的暴击率和闪避，数值为自身20%
 * @author Kubby
 */
@Component
public class NiaoZuiYiShengZS implements SkillEffect {

    @Override
    public EffectType getType() {
        return EffectType.NIAO_ZUI_YI_SHENG_ZS;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time,
                        SkillState skillState, Context context) {
        NiaoZuiYiShengZSParam param = state.getParam(NiaoZuiYiShengZSParam.class);

        List<PassiveState> passiveStates = owner.getPassiveStateByType(PassiveType.MU_SHI_FOLLOW);
        if (passiveStates.isEmpty()) {
            return;
        }

        for (PassiveState passiveState : passiveStates) {
            MuShiFollowPassiveParam copy = passiveState.getParam(MuShiFollowPassiveParam.class).copy();
            copy.setZsBuffId(param.getBuffId());
            passiveState.setParamOverride(copy);
        }

        Unit traceUnit = owner.getTraceUnit();
        if (traceUnit != null && !traceUnit.isDead()) {
            BuffFactory.addBuff(param.getBuffId(), owner, traceUnit, time, skillReport, null);
        }
    }
}
