package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.model.report.values.StateRemove;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.OwnerDiePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.RecoverPassive;
import com.pangu.logic.module.battle.service.passive.param.GongJiangDaShiZSParam;
import com.pangu.logic.module.battle.service.passive.utils.PassiveUtils;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 工匠大师·吉拉德专属装备
 * 1：每累计回复自身生命最大值10%，则提升自身攻击力5%，最多60%
 * 10：每累计回复自身生命最大值10%，则提升自身攻击力6%，最多90%
 * 20：每累计回复自身生命最大值10%，则提升自身攻击力8%，最多160%
 * 30：累计到最大值时免疫所有控制
 *
 * @author Kubby
 */
@Component
public class GongJiangDaShiZS implements RecoverPassive, OwnerDiePassive {

    @Override
    public PassiveType getType() {
        return PassiveType.GONG_JIANG_DA_SHI_ZS;
    }

    @Override
    public void recover(PassiveState passiveState, Unit owner, Unit from, long recover, int time, Context context,
                        SkillState skillState, SkillReport skillReport) {
        GongJiangDaShiZSAddition addition = passiveState
                .getAddition(GongJiangDaShiZSAddition.class, new GongJiangDaShiZSAddition());

        GongJiangDaShiZSParam param = passiveState.getParam(GongJiangDaShiZSParam.class);

        if (addition.getAddBuffs().size() >= param.getBuffOverlayLimit()) {
            return;
        }

        addition.incRecover(recover);

        double recoverPct = addition.getTotalRecover() * 1.0 / owner.getValue(UnitValue.HP_MAX);
        int addCount = (int) (recoverPct / param.getPreAddBuffRecoverRate());

        if (addCount <= 0 || addCount <= addition.getAddBuffs().size()) {
            return;
        }

        addCount = Math.min(addCount, param.getBuffOverlayLimit());

        int realAddCount = addCount - addition.getAddBuffs().size();

        for (int i = 0; i < realAddCount; i++) {
            BuffState buffState = BuffFactory.addBuff(param.getBuffId(), owner, owner, time, skillReport, null);
            addition.addBuff(buffState);
        }

        /* 累计最大值，免疫所有控制 */
        if (addCount >= param.getBuffOverlayLimit() && param.isBati()) {
            PassiveUtils.addState(owner, owner, UnitState.BA_TI, Integer.MAX_VALUE, time, passiveState, context, skillReport);
        }
    }

    @Override
    public void die(PassiveState passiveState, Unit owner, Unit attack, ITimedDamageReport timedDamageReport, int time,
                    Context context) {
        GongJiangDaShiZSAddition addition = passiveState.getAddition(GongJiangDaShiZSAddition.class);
        if (addition == null) {
            return;
        }

        List<BuffState> buffStates = addition.getAddBuffs();
        for (BuffState buffState : buffStates) {
            BuffFactory.removeBuffState(buffState, owner, time);
        }

        GongJiangDaShiZSParam param = passiveState.getParam(GongJiangDaShiZSParam.class);

        if (param.isBati() && owner.hasState(UnitState.BA_TI, time)) {
            PassiveValue passiveValue = PassiveValue.of(passiveState.getId(), owner.getId());
            owner.removeState(UnitState.BA_TI);
            passiveValue.add(new StateRemove(Collections.singletonList(UnitState.BA_TI)));
            timedDamageReport.add(time, owner.getId(), passiveValue);
        }

        passiveState.setAddition(null);
    }

    @Getter
    public class GongJiangDaShiZSAddition {

        private long totalRecover;

        private List<BuffState> addBuffs = new LinkedList<>();

        public void incRecover(long value) {
            totalRecover += value;
        }

        public void addBuff(BuffState buff) {
            addBuffs.add(buff);
        }

    }
}
