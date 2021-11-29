package com.pangu.logic.module.battle.service.passive.utils;

import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.TimedDamages;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.model.report.values.IValues;
import com.pangu.logic.module.battle.model.report.values.Mp;
import com.pangu.logic.module.battle.service.core.Unit;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 战报修改器
 */
public class SkillReportEditor {

    //修改本轮上下文中的指定目标的首个Hp变化战报数值
    public static void editHpDamageReport(ITimedDamageReport timedDamageReport, Unit target, long dmgChange, int time) {
        final Map<Integer, TimedDamages> damages = timedDamageReport.getDamages();
        if (damages == null) {
            return;
        }
        final TimedDamages timedDamages = damages.get(time);
        if (timedDamages == null) {
            return;
        }
        final List<IValues> iValues = timedDamages.queryUnitValue(target);
        final List<Hp> hpValues = new ArrayList<>();
        for (IValues iValue : iValues) {
            if (iValue instanceof Hp && ((Hp) iValue).getDamage() < 0) hpValues.add((Hp) iValue);
        }
        if (CollectionUtils.isEmpty(hpValues)) {
            return;
        }
        final Hp hp = hpValues.get(0);
        hp.setDamage(hp.getDamage() + dmgChange);
    }

    //修改本轮上下文中的指定目标的首个Mp变化战报数值
    public static void editMpChangeReport(ITimedDamageReport timedDamageReport, Unit target, long mpChange, int time) {
        final Map<Integer, TimedDamages> damages = timedDamageReport.getDamages();
        if (damages == null) {
            return;
        }
        final TimedDamages timedDamages = damages.get(time);
        if (timedDamages == null) {
            return;
        }
        final List<IValues> iValues = timedDamages.queryUnitValue(target);
        final List<Mp> mpValues = new ArrayList<>();
        for (IValues iValue : iValues) {
            if (iValue instanceof Mp && ((Mp) iValue).getMp() < 0) mpValues.add((Mp) iValue);
        }
        final Mp hp = mpValues.get(0);
        hp.setMp(hp.getMp() + mpChange);
    }
}
