package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.MpFrom;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.IValues;
import com.pangu.logic.module.battle.model.report.values.Mp;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.model.report.values.UnitValues;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.SkillReleasePassive;
import com.pangu.logic.module.battle.service.passive.param.YinBaiZhiCiZSParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

/**
 * 银白之刺·利昂专属装备
 * 1：每次释放无声突袭之后，都会获得100点能量
 * 10：同时恢复300%攻击的血量
 * 20：每次释放无声突袭之后，都会获得200点能量
 * 30：暴击提升20%，持续4秒
 * @author Kubby
 */
@Component
public class YinBaiZhiCiZS implements SkillReleasePassive {

    @Override
    public PassiveType getType() {
        return PassiveType.YIN_BAI_ZHI_CI_ZS;
    }

    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time,
                             Context context, SkillReport skillReport) {
        if (owner != attacker) {
            return;
        }

        YinBaiZhiCiZSParam param = passiveState.getParam(YinBaiZhiCiZSParam.class);

        if (!param.getSkillTag().equals(skillState.getTag())) {
            return;
        }

        List<IValues> ivalues = new LinkedList<>();

        
        if (param.getCureMpValue() > 0) {
            context.addPassiveValue(owner, AlterType.MP, param.getCureMpValue());
            ivalues.add(new Mp(param.getCureMpValue(), MpFrom.SKILL));
        }

        
        if (param.getCureHpRate() > 0) {
            long cureHp = (long) (owner.getValue(UnitValue.ATTACK_P) * param.getCureHpRate());
            context.addPassiveValue(owner, AlterType.HP, cureHp);
            ivalues.add(new UnitValues(AlterType.HP, cureHp));
        }

        
        if (!StringUtils.isBlank(param.getBuffId())) {
            BuffFactory.addBuff(param.getBuffId(), owner, owner, time, skillReport, null);
        }

        if (!ivalues.isEmpty()) {
            PassiveValue passiveValue = PassiveValue.of(passiveState.getId(), owner.getId());
            ivalues.forEach(passiveValue::add);
            skillReport.add(time, owner.getId(), passiveValue);
        }

    }
}
