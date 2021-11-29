package com.pangu.logic.module.battle.model.report;

import com.pangu.logic.module.battle.model.*;
import com.pangu.logic.module.battle.service.core.Fighter;
import com.pangu.framework.protocol.annotation.Transable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.LinkedList;
import java.util.Map;

/**
 * 单场战报
 */
@Transable
@Getter
@Setter
@ToString
public class FightReport {
    /**
     * 初始化战斗时的
     */
    private BattleInfo battleInfo;
    /**
     * 行动战报战报
     */
    private LinkedList<IReport> actions = new LinkedList<>();

    /**
     * 本场战斗结果
     */
    private BattleResult result;

    /**
     * 战报统计
     */
    private ReportSummary reportSummary;

    /**
     * 战斗时长
     */
    private int time;

    public static FightReport valueOf(Fighter attacker, Fighter defender, BattleType type, String sceneId) {
        FightReport result = new FightReport();
        result.battleInfo = BattleInfo.valueOf(type, FighterInfo.valueOf(attacker), FighterInfo.valueOf(defender), sceneId);
        return result;
    }

    public static FightReport valueOfEmptyDefender(Fighter attacker, BattleType type, String sceneId) {
        FightReport result = new FightReport();
        result.battleInfo = BattleInfo.valueOf(type, FighterInfo.valueOf(attacker), null, sceneId);
        result.result = BattleResult.ATTACKER;
        return result;
    }

    public static FightReport valueOfEmptyAttacker(Fighter defender, BattleType type, String sceneId) {
        FightReport result = new FightReport();
        result.battleInfo = BattleInfo.valueOf(type, null, FighterInfo.valueOf(defender), sceneId);
        result.result = BattleResult.DEFENDER;
        return result;
    }

    public void add(IReport report) {
        this.actions.add(report);
    }


    public void summary(Map<String, String> summerMap) {
        ReportSummary reportSummary = new ReportSummary();
        reportSummary.initUnit(battleInfo.getAttacker(), battleInfo.getDefender());
        for (IReport actionReport : actions) {
            if (actionReport instanceof SkillReport) {
                reportSummary.parse((SkillReport) actionReport, summerMap);
                continue;
            }
            if (actionReport instanceof BuffReport) {
                reportSummary.parse((BuffReport) actionReport, summerMap);
            }
        }
        this.reportSummary = reportSummary;
    }
}
