package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.resource.BuffSetting;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.param.BuffUpdateParam;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.common.resource.Formula;
import com.pangu.framework.resource.Storage;
import com.pangu.framework.resource.anno.Static;
import org.springframework.stereotype.Component;

/**
 * 主要用于光环类buff和叠层类buff的添加和更新
 */
@Component
public class BuffUpdate implements SkillEffect {
    //  技能是否施放成功
    @Static("FIGHT:BUFF:IS_SUCCESS")
    private Formula isSuccessFormula;

    @Static
    private Storage<String, BuffSetting> configs;

    @Override
    public EffectType getType() {
        return EffectType.BUFF_UPDATE;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final BuffUpdateParam param = state.getParam(BuffUpdateParam.class);
        doBuffUpdate(param, owner, target, skillReport, time);
    }

    public void doBuffUpdate(BuffUpdateParam param, Unit owner, Unit target, ITimedDamageReport timedDamageReport, int time) {
        //获取目标身上已存在的同tagBuff。若不存在便添加，若存在便更新
        final String buffId = param.getBuffId();
        final BuffSetting buffSetting = configs.get(buffId, true);
        final BuffState buffState = target.getBuffStateByTag(buffSetting.getTag());
        if (buffState != null) {
            //buff续期策略
            if (buffState.getBuffAction() == null || param.isReset()) {
                //重置策略
                BuffFactory.updateBuff(time, target, time + buffSetting.getTime(), buffState, param.getAddition());
            } else {
                //延长固定时间策略
                BuffFactory.updateBuff(time, target, buffState.getBuffAction().getRemoveTime() + param.getDelay(), buffState, param.getAddition());
            }
        } else {
            //若身上不存在buff，则首先添加buff。addition通常为COUNTER类buff计数层数
            BuffFactory.addBuff(buffId, owner, target, time, timedDamageReport, param.getAddition());
        }
    }
}
