package com.pangu.logic.module.battle.service.buff.effect;


import com.pangu.logic.module.battle.service.buff.Buff;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.buff.param.ZhuanZhuSheJiParam;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 专注射击
 * 自己贴身范围内没有敌人时,命中率提升5%,暴击率提升10%
 * 2级:额外增加30%的暴击伤害
 * 3级:命中率提升至10%
 * 4级:暴击率提升至20%
 */
@Component
public class ZhuanZhuSheJi implements Buff {
    @Override
    public BuffType getType() {
        return BuffType.ZHUAN_ZHU_SHE_JI;
    }

    @Override
    public boolean add(BuffState state, Unit unit, int time) {
        unit.addBuff(state);
        // 立即生效一次
        update(state, unit, time, null);
        return true;
    }

    @Override
    public void update(BuffState state, Unit unit, int time, Object addition) {
        final ZhuanZhuSheJiParam param = state.getParam(ZhuanZhuSheJiParam.class);
        final List<Unit> conditionUnits = TargetSelector.select(unit, param.getConditionTargetId(), time);
        //当自身周围有敌人时，不添加buff
        if (conditionUnits.size() > 0) return;
        final List<Unit> buffUnits = TargetSelector.select(unit, param.getBuffTargetId(), time);
        for (Unit buffUnit : buffUnits) {
            BuffFactory.addBuff(param.getBuffId(), unit, buffUnit, time, state.getBuffReport(), null);
        }
    }
}
