package com.pangu.logic.module.battle.service.select.sort;

import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.model.ReportSummary;
import com.pangu.logic.module.battle.model.UnitReport;
import com.pangu.logic.module.battle.model.report.FightReport;
import com.pangu.logic.module.battle.service.core.Unit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DmgLow implements SortProcessor{
    @Override
    public List<Unit> sort(Point position, List<Unit> units) {
        if (CollectionUtils.isEmpty(units)) {
            return Collections.emptyList();
        }
        final FightReport report = units.get(0).getBattle().getReport();
        report.summary(Collections.emptyMap());
        final ReportSummary reportSummary = report.getReportSummary();
        final Map<String, UnitReport> unitDmgSummary = reportSummary.getReports();
        return units.stream()
                .map(unit -> {
                    final UnitReport unitReport = unitDmgSummary.get(unit.getId());
                    if (unitReport == null) {
                        return new UnitRef(unit, 0L);
                    }
                    final long dmg = unitReport.getAttack();
                    return new UnitRef(unit, dmg);
                })
                .sorted(Comparator.comparingLong(UnitRef::getDmg))
                .map(UnitRef::getUnit)
                .collect(Collectors.toList());
    }

    @AllArgsConstructor
    @Getter
    private static class UnitRef {
        private final Unit unit;
        private final long dmg;
    }
}
