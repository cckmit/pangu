package com.pangu.logic.module.battle.service.action;

import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.MpReport;
import com.pangu.logic.module.battle.resource.BattleSetting;
import com.pangu.logic.module.battle.service.Battle;
import com.pangu.logic.module.battle.service.core.Unit;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class IntervalAddMpAction implements Action {
    private int time;
    private final Battle battle;

    public IntervalAddMpAction(int time, Battle battle) {
        this.time = time;
        this.battle = battle;
    }

    @Override
    public void execute() {
        List<Unit> attackAllLive = battle.getAttacker().getCurrent();
        List<Unit> defenderAllLive = battle.getDefender().getCurrent();
        BattleSetting config = battle.getConfig();
        int addMpValue = config.getAddMpValue();
        Map<String, Long> addMp = new HashMap<>(attackAllLive.size() + defenderAllLive.size());
        for (Unit unit : attackAllLive) {
            long curMp = unit.increaseValue(UnitValue.MP, addMpValue);
            addMp.put(unit.getId(), curMp);
        }
        for (Unit unit : defenderAllLive) {
            long curMp = unit.increaseValue(UnitValue.MP, addMpValue);
            addMp.put(unit.getId(), curMp);
        }
        battle.addReport(new MpReport(time, addMp));

        time += config.getAddMpInterval();
        battle.addWorldAction(this);
    }
}
