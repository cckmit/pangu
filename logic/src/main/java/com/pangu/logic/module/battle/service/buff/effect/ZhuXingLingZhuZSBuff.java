package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.UnitType;
import com.pangu.logic.module.battle.model.report.BuffReport;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.buff.Buff;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveFactory;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.effect.ZhuXingLingZhuZS;
import org.springframework.stereotype.Component;


/**
 * 1：大招每命中一个物理单位，生成一个火球，+4%造成的伤害/每火球。每命中一个法系单位，生成一个冰球，+7%能量恢复速率/每冰球
 * 10：+6.5%造成的伤害/每火球，+10%能量恢复速率/每冰球
 * 20：+9%造成的伤害/每火球，+15%能量恢复速率/每冰球
 * 30：下次释放末日审判时会消耗所有法球附带额外效果，并根据击中的目标重新生成法球
 * 混沌陨石：大招伤害+10%/每火球，降低命中目标10%魔防/冰球
 * 电磁脉冲：降低命中目标10%攻速/每火球，降低命中目标10%能量恢复速度/冰球
 * <p>
 * 该buff仅用于统计冰球数量
 */
@Component
public class ZhuXingLingZhuZSBuff implements Buff {
    @Override
    public BuffType getType() {
        return BuffType.ZHU_XING_LING_ZHU_ZS;
    }

    @Override
    public boolean add(BuffState state, Unit unit, int time) {
        unit.addBuff(state);
        state.setAddition(0);
        return true;
    }

    @Override
    public void update(BuffState state, Unit unit, int time, Object addition) {

        final PassiveState passiveState = unit.getPassiveStateByType(PassiveType.ZHU_XING_LING_ZHU_ZS).get(0);
        final ZhuXingLingZhuZS passive = PassiveFactory.getPassive(PassiveType.ZHU_XING_LING_ZHU_ZS);
        final ZhuXingLingZhuZS.Addition passiveAddition = passive.getAddition(passiveState);

        //获取冰球数量
        final int availableIceOrb = passiveAddition.getAvailableOrbs().get(UnitType.INTELLECT).size();
        int preIceOrb = state.getAddition(Integer.class);
        final Double mpAddRatePerIceOrb = state.getParam(Double.class);
        final int iceOrbAdd = availableIceOrb - preIceOrb;

        if (iceOrbAdd == 0) {
            return;
        }

        //根据冰球数量为自身添加BUFF
        state.setAddition(availableIceOrb);
        final double mpAddRateChange = iceOrbAdd * mpAddRatePerIceOrb;
        final Context context = new Context(state.getCaster());
        context.addValue(unit, AlterType.RATE_MP_ADD_RATE, mpAddRateChange);
        final BuffReport buffReport = state.getBuffReport();
        buffReport.add(time, unit.getId(), new UnitValues(AlterType.RATE_MP_ADD_RATE, mpAddRateChange));
        context.execute(time, buffReport);
    }
}
