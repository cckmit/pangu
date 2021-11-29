package com.pangu.logic.module.battle.model.report;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.pangu.logic.module.battle.model.BattleInfo;
import com.pangu.logic.module.battle.model.BattleResult;
import com.pangu.logic.module.battle.model.ReportSummary;
import com.pangu.framework.protocol.annotation.Transable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * 战报对象
 */
@JsonInclude(Include.NON_NULL)
@Transable
@Getter
@Setter
@ToString
public class BattleReport {
    /**
     * 战斗对应的战报
     */
    private List<FightReport> fightReports;

    /**
     * 战斗结果（总的）
     */
    private BattleResult result;

    public void addFightReport(FightReport report) {
        fightReports.add(report);
    }

    public int calWinTimes(boolean attacker) {
        int winTimes = 0;
        BattleResult result = attacker ? BattleResult.ATTACKER : BattleResult.DEFENDER;
        for (FightReport report : fightReports) {
            if (result == report.getResult()) {
                winTimes++;
            }
        }
        return winTimes;
    }

    @JsonIgnore
    public List<BattleInfo> getBattleInfo() {
        List<BattleInfo> battleInfos = new ArrayList<>(fightReports.size());
        for (FightReport fightReport : fightReports) {
            battleInfos.add(fightReport.getBattleInfo());
        }
        return battleInfos;
    }

    @JsonIgnore
    public List<ReportSummary> getReportSummary() {
        List<ReportSummary> summaries = new ArrayList<>(fightReports.size());
        for (FightReport fightReport : fightReports) {
            summaries.add(fightReport.getReportSummary());
        }
        return summaries;
    }

    public static BattleReport of(BattleResult result, int size) {
        BattleReport report = new BattleReport();
        report.result = result;
        report.fightReports = new ArrayList<>(size);
        return report;
    }

}
