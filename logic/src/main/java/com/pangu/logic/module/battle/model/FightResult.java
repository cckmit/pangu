package com.pangu.logic.module.battle.model;

import com.pangu.logic.module.battle.model.report.BattleReport;
import com.pangu.logic.module.battle.service.Battle;
import com.pangu.logic.module.battle.service.core.Fighter;
import com.pangu.logic.module.battle.service.core.Unit;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 战斗结果返回
 */
@Getter
public class FightResult {

    /**
     * 对应的战斗场次
     */
    private List<Battle> battles;

    /**
     * 最终战斗结果
     */
    private BattleResult result;

    //获取单场战斗Battle对象
    public Battle getSingleBattle() {
        if (battles.size() > 1) {
            throw new RuntimeException("当前战斗为多场战斗 无法只获取一场数据");
        }
        return battles.get(0);
    }

    public ReportSummary getSingleReportSummary() {
        return getSingleBattle().getReport().getReportSummary();
    }


    public int getWinTimes(BattleResult result) {
        int times = 0;
        for (Battle battle : battles) {
            if (battle.getResult() == result) {
                times++;
            }
        }
        return times;
    }

    public long getDefTotalHpChange() {
        long total = 0;
        for (Battle battle : battles) {
            final List<Unit> allUnit = battle.getDefender().getAllUnit();
            for (Unit unit : allUnit) {
                if (unit.isDead()) {
                    total += unit.getValue(UnitValue.HP_MAX);
                } else {
                    total += unit.getValue(UnitValue.HP_MAX) - unit.getValue(UnitValue.HP);
                }
            }
        }
        return total;
    }

    public BattleReport getBattleReport() {
        BattleReport report = BattleReport.of(result, battles.size());
        for (Battle battle : battles) {
            report.addFightReport(battle.getReport());
        }
        return report;
    }

    public List<List<Integer>> getAttackerHeroes() {
        return getUsedHeroes(true);
    }

    public List<List<Integer>> getDefenderHeroes() {
        return getUsedHeroes(false);
    }

    private List<List<Integer>> getUsedHeroes(boolean attack) {
        List<List<Integer>> list = new ArrayList<>(battles.size());
        for (Battle battle : battles) {
            Fighter fighter;
            if (attack) {
                fighter = battle.getAttacker();
            } else {
                fighter = battle.getDefender();
            }
            if (fighter == null || fighter.isEmpty()) {
                continue;
            }
            List<Integer> sortList = new ArrayList<>(6);
            List<Unit> current = new ArrayList<>(fighter.getCurrent());
            current.sort(Comparator.comparingInt(Unit::getSequence));
            for (Unit unit : current) {
                if (unit.isSummon()) {
                    continue;
                }
                final int baseId = unit.getModel().getBaseId();
                if (baseId < 0) {
                    continue;
                }
                sortList.add(baseId);
            }
            list.add(sortList);
        }
        return list;
    }

    public static FightResult of(BattleResult result, Battle battles) {
        FightResult fightResult = new FightResult();
        fightResult.battles = Collections.singletonList(battles);
        fightResult.result = result;
        return fightResult;
    }

    public static FightResult of(BattleResult result, List<Battle> battles) {
        FightResult fightResult = new FightResult();
        fightResult.battles = battles;
        fightResult.result = result;
        return fightResult;
    }

}
