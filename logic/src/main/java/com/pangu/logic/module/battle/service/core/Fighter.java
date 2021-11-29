package com.pangu.logic.module.battle.service.core;

import com.pangu.logic.module.battle.model.FighterDescribe;
import com.pangu.logic.module.battle.service.Battle;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.Phase;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * 攻击方或者防守方集合
 */
@Getter
public class Fighter {

    private static Fighter EMPTY = new EmptyFighter();

    //  攻击方标识前缀
    public static final String ATTACKER_PREFIX = "A";
    //  防守方标识前缀
    public static final String DEFENDER_PREFIX = "D";

    // 信息描述，会记录进入战报
    private FighterDescribe describe;

    //  当前的战斗单元
    private List<Unit> current;

    // 死亡单元
    private List<Unit> dieUnit;

    // 属性参考单元-施法柱单元
    private Map<Unit, Unit> servantUnit;

    // 是否是攻击方
    private boolean attacker;

    // 下一个召唤物下表
    private int nextSummonIndex = 10;

    // 场景状态监听器
    private final HashMap<Phase, Set<Unit>> listenerUnits = new HashMap<>();

    //  战场引用
    @Setter
    private Battle battle;

    public static Fighter valueOf(List<Unit> units, boolean attacker) {
        return valueOf(units, attacker, null);
    }

    public static Fighter valueOf(List<Unit> units, boolean attacker, FighterDescribe describe) {
        Fighter fighter = new Fighter();
        fighter.describe = describe;
        fighter.current = units;
        fighter.attacker = attacker;
        if (units == null) {
            return fighter;
        }
        int idIndex = 0;
        for (Unit unit : units) {
            unit.setId(Unit.toUnitId(attacker, idIndex++));
            checkListener(fighter, unit);
        }
        return fighter;
    }

    public boolean isEmpty() {
        return current == null || current.isEmpty();
    }

    private static void checkListener(Fighter fighter, Unit unit) {
        Map<Phase, List<PassiveState>> passiveStates = unit.getPassiveStates();
        HashMap<Phase, Set<Unit>> listenerUnits = fighter.listenerUnits;
        for (Map.Entry<Phase, List<PassiveState>> entry : passiveStates.entrySet()) {
            Phase phase = entry.getKey();
            List<PassiveState> list = entry.getValue();
            if (phase.listener) {
                Set<Unit> phaseUnits = listenerUnits.computeIfAbsent(phase, k -> new HashSet<>());
                if (list != null && list.size() != 0 && !unit.isDead()) {
                    phaseUnits.add(unit);
                } else {
                    phaseUnits.remove(unit);
                }
                if (phaseUnits.isEmpty()) {
                    listenerUnits.remove(phase);
                }
            }
        }
    }

    public void die(Unit unit) {
        boolean remove = current.remove(unit);
        if (remove) {
            if (dieUnit == null) {
                this.dieUnit = new ArrayList<>(6);
            }
            dieUnit.add(unit);
            checkListener(this, unit);
        }
    }

    public void revive(Unit unit) {
        if (current.contains(unit)) {
            return;
        }
        current.add(unit);
        if (dieUnit != null) {
            dieUnit.remove(unit);

        }
        checkListener(this, unit);
    }

    public boolean isDie() {
        if (current.isEmpty()) {
            return true;
        }
        for (Unit unit : current) {
            if (unit.isSummon()) {
                continue;
            }
            if (unit.isDead()) {
                continue;
            }
            return false;
        }
        return true;
    }

    public int nextSummonIndex() {
        return ++nextSummonIndex;
    }

    public void summon(Unit unit) {
        if (unit.isDead()) {
            return;
        }
        if (unit.isJoinFighter()) {
            this.current.add(unit);
        }
        battle.addUnit(unit);
    }

    public void addListener(Unit unit, Phase[] phases) {
        for (Phase phase : phases) {
            if (!phase.listener) {
                continue;
            }
            Set<Unit> phaseUnits = listenerUnits.computeIfAbsent(phase, k -> new HashSet<>());
            phaseUnits.add(unit);
        }
    }

    public List<Unit> getDieUnit() {
        if (dieUnit == null) {
            return Collections.emptyList();
        }
        return dieUnit;
    }

    public List<Unit> getAliveHeroes() {
        final List<Unit> currencies = new ArrayList<>(current.size());
        for (Unit unit : current) {
            if (unit.heroUnit()) {
                currencies.add(unit);
            }
        }
        return currencies;
    }

    public List<Unit> getDeadHeroes() {
        if (dieUnit == null) {
            return Collections.emptyList();
        }

        final List<Unit> heroes = new ArrayList<>(dieUnit.size());
        for (Unit unit : dieUnit) {
            if (unit.heroUnit()) {
                heroes.add(unit);
            }
        }
        return heroes;
    }

    public Set<Unit> getHpChangeListener() {
        return listenerUnits.get(Phase.HP_DOWN);
    }

    public Set<Unit> getUnitDieListener() {
        return listenerUnits.get(Phase.DIE);
    }

    public Set<Unit> getSkillListener() {
        return listenerUnits.get(Phase.SKILL_RELEASE);
    }

    public List<Unit> getAllUnit() {
        int size = current.size() + (dieUnit != null ? dieUnit.size() : 0);
        List<Unit> units = new ArrayList<>(size);
        units.addAll(current);
        if (dieUnit != null) {
            units.addAll(dieUnit);
        }
        return units;
    }

    public Unit getServantByMaster(Unit master) {
        if (servantUnit == null) {
            servantUnit = new HashMap<>(1);
        }
        return servantUnit.get(master);
    }

    public Unit registerServant(Unit master, Unit servant) {
        if (servantUnit == null) {
            servantUnit = new HashMap<>(1);
        }
        return servantUnit.put(master, servant);
    }

    public static Fighter emptyOf() {
        return EMPTY;
    }

    private static class EmptyFighter extends Fighter {

        @Override
        public List<Unit> getCurrent() {
            return Collections.emptyList();
        }

        @Override
        public List<Unit> getAllUnit() {
            return Collections.emptyList();
        }
    }
}
