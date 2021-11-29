package com.pangu.logic.module.battle.facade;

import com.pangu.logic.module.battle.model.BattleType;
import com.pangu.logic.module.battle.model.FightResult;
import com.pangu.logic.module.battle.model.FighterType;
import com.pangu.logic.module.battle.model.UnitBuildInfo;
import com.pangu.logic.module.battle.model.report.BattleReport;
import com.pangu.logic.module.battle.service.BattleService;
import com.pangu.logic.module.battle.service.convertor.Battler;
import com.pangu.framework.utils.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class BattleFacadeImpl implements BattleFacade {

    @Autowired
    private BattleService battleService;

    @Override
    public Result<BattleReport> test(String attacker, String defenser) {
        Battler<String> attack = Battler.valueOf(FighterType.ENEMY, attacker);
        Battler<String> defense = Battler.valueOf(FighterType.ENEMY, defenser);
        FightResult fightResult = battleService.start(BattleType.NORMAL, attack, defense);
        BattleReport report = fightResult.getBattleReport();
        return Result.SUCCESS(report);
    }

    @Override
    public Result<BattleReport> testUnit(UnitBuildInfo[][] attacker, UnitBuildInfo[][] defender) {
        Battler<UnitBuildInfo[][]> attack = Battler.valueOf(FighterType.ENEMY_UNIT, attacker);
        Battler<UnitBuildInfo[][]> defend = Battler.valueOf(FighterType.ENEMY_UNIT, defender);
        FightResult fightResult = battleService.start(BattleType.NORMAL, attack, defend);
        BattleReport report = fightResult.getBattleReport();
        return Result.SUCCESS(report);
    }

    @Override
    public Result<BattleReport> testUnitId(String[] attacker, String[] defender) {
        Battler<String[]> attack = Battler.valueOf(FighterType.ENEMY_UNIT_ID, attacker);
        Battler<String[]> defend = Battler.valueOf(FighterType.ENEMY_UNIT_ID, defender);
        FightResult fightResult = battleService.start(BattleType.NORMAL, attack, defend);
        BattleReport report = fightResult.getBattleReport();
        return Result.SUCCESS(report);
    }
}
