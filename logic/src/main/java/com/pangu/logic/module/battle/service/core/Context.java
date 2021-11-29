package com.pangu.logic.module.battle.service.core;

import com.pangu.logic.module.battle.model.*;
import com.pangu.logic.module.battle.model.report.*;
import com.pangu.logic.module.battle.model.report.values.*;
import com.pangu.logic.module.battle.resource.BattleSetting;
import com.pangu.logic.module.battle.service.action.Action;
import com.pangu.logic.module.battle.service.action.SkillAction;
import com.pangu.logic.module.battle.service.action.SkillEffectAction;
import com.pangu.logic.module.battle.service.buff.param.DefaultAddValueParam;
import com.pangu.logic.module.battle.service.buff.utils.CalTypeHelper;
import com.pangu.logic.module.battle.service.buff.utils.CalValues;
import com.pangu.logic.module.battle.service.passive.*;
import com.pangu.logic.module.battle.service.skill.CommonFormula;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.effect.HpHigherDamage;
import com.pangu.logic.module.battle.service.skill.effect.HpRecover;
import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import com.pangu.logic.module.battle.service.skill.param.HpRecoverParam;
import com.pangu.framework.utils.math.RandomUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.util.*;

@Getter
public class Context {

    // 技能修改属性
    private final Map<Unit, AlterValue> origin = new HashMap<>(12);

    // 被动修改属性
    private final Map<Unit, AlterValue> passiveValues = new HashMap<>(6);

    // 总修改属性
    private final Map<Unit, AlterValue> totalValues = new HashMap<>(12);

    // 本轮上下文中，参与上下文计算的所有单元的初始生命值
    private final Map<Unit, Long> initHp = new HashMap<>(6);

    // 状态变更
    private Map<Unit, UnitStateContext> unitStates;

    // 召唤物
    private List<Unit> summonUnits;

    // 目标状态
    private Map<Unit, TargetDamageTag> damageTags;

    // 上下文中需要使用的技能效果
    private Map<EffectType, SkillEffect> type2SkillEffects;

    // 如果是循环执行
    @Setter
    private int loopTimes;

    // 目标总数
    @Setter
    private int targetAmount;

    // 攻击方
    private final Unit attacker;

    // 供被动获取技能循环间共享数据的桥梁
    @Setter
    private SkillEffectAction rootSkillEffectAction;

    //当前是否为执行被动阶段（如果是则当前阶段所有添加的属性变更都 放在passiveValues）
    private boolean execPassive;

    //本轮上下文缓存的额外数据
    private Map<String, Object> addition;

    public Context(Unit attacker) {
        this.attacker = attacker;
    }

    public void addValue(Unit unit, AlterType type, Number value) {
        if (execPassive) {
            addPassiveValue(unit, type, value);
            return;
        }
        AlterValue alterValue = origin.computeIfAbsent(unit, k -> new AlterValue());
        alterValue.addValue(type, value);
    }

    public void addPassiveValue(Unit unit, AlterType type, Number value) {
        AlterValue alterValue = passiveValues.computeIfAbsent(unit, k -> new AlterValue());
        alterValue.addValue(type, value);
    }

    Map<String, AlterAfterValue> execute(int time, Set<Unit> dieUnits, Set<Unit> hpChangeUnits, boolean confirmDeath) {
        Map<String, AlterAfterValue> afterValues = new HashMap<>(origin.size() + 1);
        for (Map.Entry<Unit, AlterValue> entry : origin.entrySet()) {
            Unit unit = entry.getKey();

            long preHp = unit.getValue(UnitValue.HP);

            AlterValue alterValue = entry.getValue();
            AlterValue passiveValue = passiveValues.remove(unit);
            if (passiveValue != null) {
                alterValue.addValue(passiveValue);
            }
            AlterAfterValue afterValue = new AlterAfterValue();
            afterValues.put(unit.getId(), afterValue);

            alterValue.execute(unit, afterValue, time);

            long curHp = unit.getValue(UnitValue.HP);
            if (curHp != preHp) {
                this.initHp.putIfAbsent(unit, preHp);
                hpChangeUnits.add(unit);
            }

            if (curHp <= 0 || unit.isDead()) {
                dieUnits.add(unit);
            }
        }
        for (Map.Entry<Unit, AlterValue> entry : passiveValues.entrySet()) {
            Unit unit = entry.getKey();

            long preHp = unit.getValue(UnitValue.HP);

            AlterValue alterValue = entry.getValue();
            AlterAfterValue afterValue = afterValues.computeIfAbsent(unit.getId(), k -> new AlterAfterValue());
            alterValue.execute(unit, afterValue, time);

            long curHp = unit.getValue(UnitValue.HP);
            if (curHp != preHp) {
                this.initHp.putIfAbsent(unit, preHp);
                hpChangeUnits.add(unit);
            }

            if (curHp <= 0 || unit.isDead()) {
                dieUnits.add(unit);
            }
        }

        if (confirmDeath) {
            final Iterator<Unit> it = dieUnits.iterator();
            while (it.hasNext()) {
                final Unit dieCandidate = it.next();
                if (dieCandidate.getValue(UnitValue.HP) <= 0 || dieCandidate.isDead()) {
                    dieCandidate.dead();
                    if (dieCandidate.isDiePassiveTriggered()) {
                        it.remove();
                        continue;
                    }
                    dieCandidate.setDiePassiveTriggered(true);
                } else {
                    it.remove();
                }
            }
        }

        if (summonUnits != null) {
            for (Unit unit : summonUnits) {
                unit.reset(time);
                Fighter friend = unit.getFriend();
                friend.summon(unit);
            }
        }
        saveAndClearCurrentChangeValues();
        if (summonUnits != null) {
            summonUnits.clear();
        }
        return afterValues;
    }

    private void mergeAlteredValue(Map<String, AlterAfterValue> base, Map<String, AlterAfterValue> newAltered) {
        for (Map.Entry<String, AlterAfterValue> newAlt : newAltered.entrySet()) {
            final String unitId = newAlt.getKey();
            final AlterAfterValue unitVal = newAlt.getValue();
            base.merge(unitId, unitVal, (oldValue, newValue) -> {
                oldValue.merge(newValue);
                return oldValue;
            });
        }
    }

    private void calcDamageMp(int time, Set<Unit> hpChangeUnit, ITimedDamageReport report) {
        if (hpChangeUnit.isEmpty()) {
            return;
        }
        BattleSetting config = attacker.getBattle().getConfig();
        int damagePercent = config.getDamagePercent();
        if (damagePercent <= 0) {
            return;
        }
        final Map<Integer, TimedDamages> damages = report.getDamages();
        if (damages == null) {
            return;
        }
        TimedDamages timedDamages = damages.get(time);
        if (timedDamages == null) {
            return;
        }
        int damageMp = config.getDamageMp();
        for (Unit curUnit : hpChangeUnit) {
            long hp = curUnit.getValue(UnitValue.HP);
            if (hp <= 0) {
                continue;
            }
            long hpMax = curUnit.getValue(UnitValue.HP_MAX);

            long cur = (hpMax - hp) * 100 / hpMax / damagePercent;

            long lastDamageMpPercent = curUnit.getLastDamageMpPercent();
            curUnit.setLastDamageMpPercent(cur);

            if (cur <= lastDamageMpPercent) {
                continue;
            }
            long originAddMp = damageMp * (cur - lastDamageMpPercent) + curUnit.getValue(UnitValue.DAMAGE_MP_ADD);
            if (originAddMp <= 0) {
                continue;
            }
            double addRate = 1 + curUnit.getRate(UnitRate.DAMAGE_MP_ADD) + curUnit.getRate(UnitRate.MP_ADD_RATE);
            if (addRate <= 0) {
                continue;
            }
            long addMp = (long) (originAddMp * addRate);
            if (addMp == 0) {
                continue;
            }


            //对初始化技能造成的伤害进行特殊处理，不进行能量回复
            if (time == 0) {
                return;
            }

            long curMp = curUnit.increaseValue(UnitValue.MP, addMp);

            report.add(time, curUnit.getId(), new Mp(addMp, MpFrom.DAMAGE));

            AlterAfterValue afterValue = takeAlterValue(timedDamages, curUnit);
            afterValue.put(UnitValue.MP, curMp);
        }
    }

    private AlterAfterValue takeAlterValue(TimedDamages timedDamages, Unit curUnit) {
        Map<String, AlterAfterValue> afterValues = timedDamages.getAfterValues();
        AlterAfterValue afterValue;
        if (afterValues == null) {
            afterValue = new AlterAfterValue();
            timedDamages.addAfterValue(Collections.singletonMap(curUnit.getId(), afterValue));
        } else {
            afterValue = afterValues.get(curUnit.getId());
            if (afterValue == null) {
                afterValue = new AlterAfterValue();
                afterValues.put(curUnit.getId(), afterValue);
            }
        }
        return afterValue;
    }

    private void calcKilledMp(int time, Set<Unit> dieUnit, ITimedDamageReport report) {
        if (dieUnit.size() <= 0) {
            return;
        }
        BattleSetting config = attacker.getBattle().getConfig();
        long count = 0;
        int killMp = config.getKillAddMp();
        for (Unit unit : dieUnit) {
            if (!unit.isDead()) {
                continue;
            }
            count += killMp;
        }
        long originAddMp = count + attacker.getValue(UnitValue.KILL_MP_ADD);
        if (originAddMp <= 0) {
            return;
        }
        double addRate = 1 + attacker.getRate(UnitRate.KILL_MP_ADD) + attacker.getRate(UnitRate.MP_ADD_RATE);
        if (addRate <= 0) {
            return;
        }
        long curMp = attacker.increaseValue(UnitValue.MP, count);

        final Map<Integer, TimedDamages> damages = report.getDamages();
        if (damages == null) {
            return;
        }
        final TimedDamages timedDamages = damages.get(time);
        if (timedDamages == null) {
            return;
        }
        AlterAfterValue afterValue = takeAlterValue(timedDamages, attacker);
        afterValue.put(UnitValue.MP, curMp);

        report.add(time, attacker.getId(), new Mp(count, MpFrom.KILL));
    }

    private void executeState(int time, ITimedDamageReport damageReport) {
        if (unitStates == null) {
            return;
        }
        for (Map.Entry<Unit, UnitStateContext> entry : unitStates.entrySet()) {
            Unit unit = entry.getKey();
            UnitStateContext unitStateContext = entry.getValue();
            Map<UnitState, Integer> harmState = unitStateContext.getHarmState();
            if (harmState != null) {
                effect(unit, harmState, time, damageReport);
                harmState.clear();
            }
            Map<UnitState, Integer> normalState = unitStateContext.getNormalState();
            if (normalState != null) {
                effect(unit, normalState, time, damageReport);
                normalState.clear();
            }
        }
        if (unitStates != null) {
            unitStates.clear();
        }
    }

    private void effect(Unit unit, Map<UnitState, Integer> states, int time, ITimedDamageReport damageReport) {
        for (Map.Entry<UnitState, Integer> entry : states.entrySet()) {
            UnitState key = entry.getKey();
            Integer value = entry.getValue();
            unit.addState(key, value);
            Action action = unit.getAction();
            if (action instanceof SkillAction) {
                if (key == UnitState.DISABLE || key == UnitState.EXILE) {
                    ((SkillAction) action).broken(time);
                }
                SkillState skillState = ((SkillAction) action).getSkillState();
                SkillType type = skillState.getType();
                if (type == SkillType.NORMAL) {
                    // 麻痹状态无法释放普攻
                    if (key == UnitState.PALSY) {
                        ((SkillAction) action).broken(time);
                    }
                } else {
                    // 沉默状态无法释放大招和技能
                    if (type == SkillType.SKILL && (key == UnitState.CHAOS || key == UnitState.SILENT)) {
                        ((SkillAction) action).broken(time);
                    }
                }
            }
        }
    }

    public Integer stateValidTimeModifier(UnitState unitState, Integer validTime, Integer time, Unit target) {
        if (unitState.harm) {
            int validDuration = validTime - time;
            validDuration = (int) (validDuration * Math.max(0, (1 - target.getRate(UnitRate.STATE_HARM_DEC))));
            validTime = validDuration + time;
        }
        return validTime;
    }

    public void executeSkillPassive(int time, SkillState skillState, Unit owner, SkillReport skillReport) {
        final double rate = owner.getRate(UnitRate.SUCK);

        for (Map.Entry<Unit, AlterValue> entry : origin.entrySet()) {
            Unit unit = entry.getKey();
            AlterValue alterValue = entry.getValue();
            long hpChange = alterValue.getLongValue(AlterType.HP);
            if (hpChange == 0) {
                continue;
            }
            // 执行伤害被动
            if (hpChange > 0) {
                executeRecoverTargetPassive(time, owner, unit, hpChange, skillState, skillReport);
                executeRecoverPassive(time, owner, unit, hpChange, skillState, skillReport);
                continue;
            }
            // 执行攻击方被动
            executeAttackPassive(time, owner, unit, hpChange, skillState, skillReport);
            // 执行被攻击被动
            executeDamagePassive(time, owner, unit, hpChange, skillState, skillReport);
            //执行攻击方结算后被动
            executeAttackEndPassive(time, owner, unit, hpChange, skillState, skillReport);

            if (rate > 0) {
                final long change = getHpChange(unit);
                if (change < 0) {
                    long value = (long) (-change * rate);
                    addPassiveValue(owner, AlterType.HP, value);
                    skillReport.add(time, owner.getId(), Hp.of(value));
                }
            }
        }
    }

    private void executeRecoverPassive(int time, Unit owner, Unit unit, long hpChange, SkillState skillState, SkillReport skillReport) {
        List<PassiveState> passiveStates = unit.getRecoverPassive();
        if (passiveStates.isEmpty()) {
            return;
        }
        PassiveState[] array = passiveStates.toArray(new PassiveState[0]);
        for (PassiveState passiveState : array) {
            if (passiveState.invalid(time)) {
                continue;
            }
            if (passiveState.shouldRemove(time)) {
                unit.removePassive(passiveState);
                continue;
            }
            PassiveType passiveType = passiveState.getType();
            RecoverPassive damagePassive = PassiveFactory.getPassive(passiveType);
            // 执行被动
            damagePassive.recover(passiveState, unit, owner, hpChange, time, this, skillState, skillReport);
            if (passiveState.shouldRemove(time)) {
                unit.removePassive(passiveState);
            }
        }
    }

    private void executeRecoverTargetPassive(int time, Unit owner, Unit unit, long hpChange, SkillState skillState, SkillReport skillReport) {
        List<PassiveState> passiveStates = owner.getRecoverTargetPassive();
        if (passiveStates.isEmpty()) {
            return;
        }
        PassiveState[] array = passiveStates.toArray(new PassiveState[0]);
        for (PassiveState passiveState : array) {
            if (passiveState.invalid(time)) {
                continue;
            }
            if (passiveState.shouldRemove(time)) {
                owner.removePassive(passiveState);
                continue;
            }
            PassiveType passiveType = passiveState.getType();
            RecoverTargetPassive damagePassive = PassiveFactory.getPassive(passiveType);
            // 执行被动
            damagePassive.recoverTarget(passiveState, owner, unit, hpChange, time, this, skillState, skillReport);
            if (passiveState.shouldRemove(time)) {
                owner.removePassive(passiveState);
            }
        }
    }

    private void executeAttackPassive(int time, Unit owner, Unit unit, long damage, SkillState skillState, SkillReport skillReport) {
        List<PassiveState> passiveStates = owner.getAttackPassive();
        if (passiveStates == null || passiveStates.isEmpty()) {
            return;
        }
        PassiveState[] array = passiveStates.toArray(new PassiveState[0]);
        for (PassiveState passiveState : array) {
            if (passiveState.invalid(time)) {
                continue;
            }
            if (passiveState.shouldRemove(time)) {
                owner.removePassive(passiveState);
                continue;
            }

            PassiveType passiveType = passiveState.getType();
            AttackPassive damagePassive = PassiveFactory.getPassive(passiveType);
            // 执行被动
            damagePassive.attack(passiveState, owner, unit, damage, time, this, skillState, skillReport);
            if (passiveState.shouldRemove(time)) {
                owner.removePassive(passiveState);
            }
        }
    }

    private void executeAttackEndPassive(int time, Unit owner, Unit unit, long damage, SkillState skillState, SkillReport skillReport) {
        List<PassiveState> passiveStates = owner.getAttackEndPassive();
        if (passiveStates == null || passiveStates.isEmpty()) {
            return;
        }
        PassiveState[] array = passiveStates.toArray(new PassiveState[0]);
        for (PassiveState passiveState : array) {
            if (passiveState.invalid(time)) {
                continue;
            }
            if (passiveState.shouldRemove(time)) {
                owner.removePassive(passiveState);
                continue;
            }
            PassiveType passiveType = passiveState.getType();
            AttackEndPassive damagePassive = PassiveFactory.getPassive(passiveType);
            // 执行被动
            damagePassive.attackEnd(passiveState, owner, unit, damage, time, this, skillState, skillReport);
            if (passiveState.shouldRemove(time)) {
                owner.removePassive(passiveState);
            }
        }
    }


    private void executeDamagePassive(int time, Unit owner, Unit unit, long damage, SkillState skillState, SkillReport skillReport) {
        List<PassiveState> passiveStates = unit.getDamagePassive();
        if (passiveStates == null || passiveStates.isEmpty()) {
            return;
        }
        PassiveState[] array = passiveStates.toArray(new PassiveState[0]);

        for (PassiveState passiveState : array) {
            if (passiveState.invalid(time)) {
                continue;
            }
            if (passiveState.shouldRemove(time)) {
                unit.removePassive(passiveState);
                continue;
            }
            PassiveType passiveType = passiveState.getType();
            DamagePassive damagePassive = PassiveFactory.getPassive(passiveType);
            // 执行被动
            damagePassive.damage(passiveState, unit, damage, owner, time, this, skillState, skillReport);
            // 删除无用的buff
            if (passiveState.shouldRemove(time)) {
                unit.removePassive(passiveState);
            }
        }
    }

    public void execute(int time, SkillReport skillReport) {

        Set<Unit> dieUnits = new HashSet<>(origin.size());
        Set<Unit> hpChangeUnits = new HashSet<>(origin.size());

        Map<String, AlterAfterValue> execute = execute(time, dieUnits, hpChangeUnits, false);
        executeState(time, skillReport);

        skillReport.addAfterValue(time, execute);
    }

    public void execute(int time, SkillState skillState, SkillReport skillReport) {

        executeSkillPassive(time, skillState, attacker, skillReport);

        Set<Unit> dieUnits = new HashSet<>(origin.size());
        Set<Unit> hpChangeUnits = new HashSet<>(origin.size());

        Map<String, AlterAfterValue> afterSkillAtk = execute(time, dieUnits, hpChangeUnits, false);

        if (hpChangeUnits.size() != 0) {
            Unit unit = hpChangeUnits.iterator().next();
            Fighter fighterOne = unit.getFriend();
            Fighter fighterTwo = unit.getEnemy();

            executeHpChangePassive(time, skillReport, hpChangeUnits, fighterOne);
            executeHpChangePassive(time, skillReport, hpChangeUnits, fighterTwo);
            calcDamageMp(time, hpChangeUnits, skillReport);
        }

        Map<String, AlterAfterValue> afterUnitHpChange = execute(time, dieUnits, hpChangeUnits, true);
        mergeAlteredValue(afterSkillAtk, afterUnitHpChange);


        if (dieUnits.size() != 0) {
            Unit unit = dieUnits.iterator().next();
            Fighter fighterOne = unit.getFriend();
            Fighter fighterTwo = unit.getEnemy();

            executeUnitDiePassive(time, skillReport, dieUnits, fighterOne);
            executeUnitDiePassive(time, skillReport, dieUnits, fighterTwo);
            calcKilledMp(time, dieUnits, skillReport);
        }

        executeOwnerDiePassive(time, skillReport, dieUnits);
        dieUnits.clear();
        hpChangeUnits.clear();
        while (true) {
            Map<String, AlterAfterValue> secondAfter = execute(time, dieUnits, hpChangeUnits, true);
            mergeAlteredValue(afterSkillAtk, secondAfter);

            calcDamageMp(time, hpChangeUnits, skillReport);
            calcKilledMp(time, dieUnits, skillReport);

            if (dieUnits.isEmpty()) {
                break;
            }
            executeOwnerDiePassive(time, skillReport, dieUnits);
            dieUnits.clear();
            hpChangeUnits.clear();
        }


        executeState(time, skillReport);

        skillReport.addAfterValue(time, afterSkillAtk);
    }

    public void execute(int time, BuffReport buffReport) {
        Set<Unit> dieUnits = new HashSet<>(origin.size());
        Set<Unit> hpChangeUnits = new HashSet<>(origin.size());
        Map<String, AlterAfterValue> afterBuffExec = execute(time, dieUnits, hpChangeUnits, false);
        if (hpChangeUnits.size() != 0) {
            Unit unit = hpChangeUnits.iterator().next();
            Fighter fighterOne = unit.getFriend();
            Fighter fighterTwo = unit.getEnemy();

            executeHpChangePassive(time, buffReport, hpChangeUnits, fighterOne);
            executeHpChangePassive(time, buffReport, hpChangeUnits, fighterTwo);

            calcDamageMp(time, hpChangeUnits, buffReport);
        }

        Map<String, AlterAfterValue> afterUnitChangeExec = execute(time, dieUnits, hpChangeUnits, true);
        mergeAlteredValue(afterBuffExec, afterUnitChangeExec);

        if (dieUnits.size() != 0) {
            Unit unit = dieUnits.iterator().next();
            Fighter fighterOne = unit.getFriend();
            Fighter fighterTwo = unit.getEnemy();

            executeUnitDiePassive(time, buffReport, dieUnits, fighterOne);
            executeUnitDiePassive(time, buffReport, dieUnits, fighterTwo);

            calcKilledMp(time, dieUnits, buffReport);
        }
        executeOwnerDiePassive(time, buffReport, dieUnits);
        dieUnits.clear();
        hpChangeUnits.clear();

        while (true) {
            Map<String, AlterAfterValue> secondAfter = execute(time, dieUnits, hpChangeUnits, true);
            mergeAlteredValue(afterBuffExec, secondAfter);

            calcDamageMp(time, hpChangeUnits, buffReport);
            calcKilledMp(time, dieUnits, buffReport);

            if (dieUnits.isEmpty()) {
                break;
            }
            executeOwnerDiePassive(time, buffReport, dieUnits);
            dieUnits.clear();
            hpChangeUnits.clear();
        }

        executeState(time, buffReport);

        buffReport.addAfterValue(time, afterBuffExec);
    }

    private void executeUnitDiePassive(int time, ITimedDamageReport timedDamageReport, Set<Unit> dieUnits, Fighter fighter) {
        Set<Unit> units = fighter.getUnitDieListener();
        if (units == null || units.size() == 0) {
            return;
        }
        Unit[] unitArray = units.toArray(new Unit[0]);
        for (Unit curUnit : unitArray) {
            List<PassiveState> passiveStates = curUnit.getUnitDiePassives();
            if (passiveStates != null && passiveStates.size() != 0) {
                PassiveState[] array = passiveStates.toArray(new PassiveState[0]);
                for (PassiveState passiveState : array) {
                    if (passiveState.invalid(time)) {
                        continue;
                    }
                    if (passiveState.shouldRemove(time)) {
                        curUnit.removePassive(passiveState);
                        continue;
                    }
                    PassiveType passiveType = passiveState.getType();
                    UnitDiePassive unitDiePassive = PassiveFactory.getPassive(passiveType);
                    // 执行被动
                    unitDiePassive.die(passiveState, curUnit, attacker, time, this, timedDamageReport, dieUnits);
                    if (passiveState.shouldRemove(time)) {
                        curUnit.removePassive(passiveState);
                        if (passiveStates.size() == 0) {
                            units.remove(curUnit);
                        }
                    }
                }
            }
        }
    }

    private void executeHpChangePassive(int time, ITimedDamageReport timedDamageReport, Set<Unit> hpChangeUnits, Fighter fighterOne) {
        Set<Unit> units = fighterOne.getHpChangeListener();
        if (units == null || units.size() == 0) {
            return;
        }
        Unit[] unitArray = units.toArray(new Unit[0]);
        for (Unit curUnit : unitArray) {
            List<PassiveState> passiveStates = curUnit.getHpChangePassives();
            if (passiveStates != null && passiveStates.size() != 0) {
                PassiveState[] array = passiveStates.toArray(new PassiveState[0]);
                for (PassiveState passiveState : array) {
                    if (passiveState.invalid(time)) {
                        continue;
                    }
                    if (passiveState.shouldRemove(time)) {
                        curUnit.removePassive(passiveState);
                        continue;
                    }
                    PassiveType passiveType = passiveState.getType();
                    UnitHpChangePassive changePassive = PassiveFactory.getPassive(passiveType);
                    // 执行被动
                    changePassive.hpChange(passiveState, curUnit, attacker, time, this, timedDamageReport, hpChangeUnits);
                    if (passiveState.shouldRemove(time)) {
                        curUnit.removePassive(passiveState);
                        if (passiveStates.size() == 0) {
                            units.remove(curUnit);
                        }
                    }
                }
            }
        }
    }

    private void executeOwnerDiePassive(int time, ITimedDamageReport timedDamageReport, Set<Unit> dieUnits) {
        for (Unit dieUnit : dieUnits) {
            final List<PassiveState> passives = dieUnit.getOwnerDiePassives();
            if (passives == null || passives.isEmpty()) {
                continue;
            }
            PassiveState[] array = passives.toArray(new PassiveState[0]);
            for (PassiveState passive : array) {
                if (passive.invalid(time)) {
                    continue;
                }
                if (passive.shouldRemove(time)) {
                    dieUnit.removePassive(passive);
                    continue;
                }
                OwnerDiePassive diePassive = PassiveFactory.getPassive(passive.getType());
                diePassive.die(passive, dieUnit, attacker, timedDamageReport, time, this);
                if (passive.shouldRemove(time)) {
                    dieUnit.removePassive(passive);
                }
            }
        }
    }

    public void addSummon(List<Unit> summonUnits) {
        if (summonUnits == null || summonUnits.isEmpty()) {
            return;
        }
        if (this.summonUnits == null) {
            this.summonUnits = new ArrayList<>(summonUnits);
        } else {
            this.summonUnits.addAll(summonUnits);
        }
    }

    /**
     * 获取本轮上下文在当前结算周期之间所受的伤害（包含无敌和护盾所免疫的伤害）
     */
    public long getHpChange(Unit unit) {
        AlterValue alterValue = origin.get(unit);
        long hp = 0;
        if (alterValue != null) {
            hp += alterValue.getLongValue(AlterType.HP);
        }
        AlterValue passiveAlter = passiveValues.get(unit);
        if (passiveAlter != null) {
            hp += passiveAlter.getLongValue(AlterType.HP);
        }
        return hp;
    }

    /**
     * 获取当前上下文中目标所受的总伤害（包含无敌和护盾所免疫的伤害）
     */
    public long getTotalHpChange(Unit unit) {
        long hp = getHpChange(unit);
        final AlterValue value = totalValues.get(unit);
        if (value != null) {
            hp += value.getLongValue(AlterType.HP);
        }
        return hp;
    }

    /**
     * 获取当前上下文中，目标的实际生命值变化
     */
    public long getActualHpChange(Unit unit) {
        final long initHp = getInitHp(unit);
        return unit.getValue(UnitValue.HP) - initHp;
    }

    public long getInitHp(Unit unit) {
        Long initHp = this.initHp.get(unit);
        if (initHp == null) {
            initHp = unit.getValue(UnitValue.HP);
        }
        return initHp;
    }

    private void saveAndClearCurrentChangeValues() {
        this.origin.forEach((k, v) -> totalValues.merge(k, v, (o, n) -> {
            o.addValue(n);
            return o;
        }));
        this.passiveValues.forEach((k, v) -> totalValues.merge(k, v, (o, n) -> {
            o.addValue(n);
            return o;
        }));
        this.origin.clear();
        this.passiveValues.clear();
    }

    public long getOriginHpChange(Unit unit) {
        AlterValue alterValue = origin.get(unit);
        long hp = 0;
        if (alterValue != null) {
            hp += alterValue.getLongValue(AlterType.HP);
        }
        return hp;
    }

    public void executeAttackBeforePassiveStart(int time, Unit target, EffectState effectState, SkillReport skillReport) {
        List<PassiveState> passiveStates = attacker.getAttackBeforePassive();
        if (passiveStates == null || passiveStates.isEmpty()) {
            return;
        }
        PassiveState[] array = passiveStates.toArray(new PassiveState[0]);
        for (PassiveState passiveState : array) {
            if (passiveState.invalid(time)) {
                continue;
            }
            if (passiveState.shouldRemove(time)) {
                attacker.removePassive(passiveState);
                continue;
            }
            PassiveType passiveType = passiveState.getType();
            AttackBeforePassive damagePassive = PassiveFactory.getPassive(passiveType);
            // 执行被动
            damagePassive.attackBefore(passiveState, effectState, attacker, target, time, this, skillReport);
            if (passiveState.shouldRemove(time)) {
                attacker.removePassive(passiveState);
            }
        }
    }

    public void executeAttackBeforePassiveEnd(int time, Unit target, EffectState effectState, SkillReport skillReport) {
        List<PassiveState> passiveStates = attacker.getAttackBeforePassive();
        if (passiveStates == null || passiveStates.isEmpty()) {
            return;
        }
        PassiveState[] array = passiveStates.toArray(new PassiveState[0]);
        for (PassiveState passiveState : array) {
            if (passiveState.invalid(time)) {
                continue;
            }
            if (passiveState.shouldRemove(time)) {
                attacker.removePassive(passiveState);
                continue;
            }
            PassiveType passiveType = passiveState.getType();
            AttackBeforePassive damagePassive = PassiveFactory.getPassive(passiveType);
            // 执行被动
            damagePassive.attackEnd(passiveState, effectState, attacker, target, time, this, skillReport);
            if (passiveState.shouldRemove(time)) {
                attacker.removePassive(passiveState);
            }
        }

    }

    public void executeBeAttackBeforePassiveStart(int time, Unit beAttackUnit, EffectState effectState, SkillReport skillReport) {
        List<PassiveState> passiveStates = beAttackUnit.getBeAttackBeforePassive();
        if (passiveStates == null || passiveStates.isEmpty()) {
            return;
        }
        PassiveState[] array = passiveStates.toArray(new PassiveState[0]);
        for (PassiveState passiveState : array) {
            if (passiveState.invalid(time)) {
                continue;
            }
            if (passiveState.shouldRemove(time)) {
                beAttackUnit.removePassive(passiveState);
                continue;
            }
            PassiveType passiveType = passiveState.getType();
            BeAttackBeforePassive damagePassive = PassiveFactory.getPassive(passiveType);
            // 执行被动
            damagePassive.beAttackBefore(passiveState, effectState, beAttackUnit, attacker, time, this, skillReport);
            if (passiveState.shouldRemove(time)) {
                beAttackUnit.removePassive(passiveState);
            }
        }
    }

    public void executeBeAttackBeforePassiveEnd(int time, Unit beAttackUnit, EffectState effectState, SkillReport skillReport) {
        List<PassiveState> passiveStates = beAttackUnit.getBeAttackBeforePassive();
        if (passiveStates == null || passiveStates.isEmpty()) {
            return;
        }
        PassiveState[] array = passiveStates.toArray(new PassiveState[0]);
        for (PassiveState passiveState : array) {
            if (passiveState.invalid(time)) {
                continue;
            }
            if (passiveState.shouldRemove(time)) {
                beAttackUnit.removePassive(passiveState);
                continue;
            }
            PassiveType passiveType = passiveState.getType();
            BeAttackBeforePassive damagePassive = PassiveFactory.getPassive(passiveType);
            // 执行被动
            damagePassive.beAttackEnd(passiveState, effectState, beAttackUnit, attacker, time, this, skillReport);
            if (passiveState.shouldRemove(time)) {
                beAttackUnit.removePassive(passiveState);
            }
        }
    }

    public void execAttackBeforePassiveAndEffect(int time, SkillEffect skillEffect, Unit target, SkillState skillState, EffectState effectState, SkillReport skillReport) {
        if (target.isDead()) {
            return;
        }
        this.executeAttackBeforePassiveStart(time, target, effectState, skillReport);
        this.executeBeAttackBeforePassiveStart(time, target, effectState, skillReport);

        // 效果释放者为敌方且效果未标记为[跳过命中校验]时，进行命中校验
        if (target.getFriend() != attacker.getFriend() && !effectState.isSkipHitCheck()) {
            // 判断是否命中
            boolean hit;
            if (target.hasState(UnitState.NO_DODGE, time)) {
                hit = true;
            } else {
                hit = CommonFormula.isHit(time, attacker, target);
            }
            if (!hit) {
                skillReport.add(time, target.getId(), new Miss());
                this.setMiss(target, true);
                this.executeAttackBeforePassiveEnd(time, target, effectState, skillReport);
                this.executeBeAttackBeforePassiveEnd(time, target, effectState, skillReport);
                return;
            }
        }
        this.setExecPassive(false);
        skillEffect.execute(effectState, attacker, target, skillReport, time, skillState, this);
        this.setExecPassive(true);
        this.executeAttackBeforePassiveEnd(time, target, effectState, skillReport);
        this.executeBeAttackBeforePassiveEnd(time, target, effectState, skillReport);
    }

    public int executeBeStateAddPassiveBefore(Unit owner, UnitState state, int time, int validTime, ITimedDamageReport damageReport) {
        List<PassiveState> passiveStates = owner.getBeStateAddPassive();
        if (passiveStates == null || passiveStates.isEmpty()) {
            return validTime;
        }

        int preValidTime = validTime;
        PassiveState[] array = passiveStates.toArray(new PassiveState[0]);
        for (PassiveState passiveState : array) {
            if (passiveState.invalid(time)) {
                continue;
            }
            if (passiveState.shouldRemove(time)) {
                owner.removePassive(passiveState);
                continue;
            }
            PassiveType passiveType = passiveState.getType();
            BeStateAddPassive beStateAddPassive = PassiveFactory.getPassive(passiveType);
            // 执行被动
            preValidTime = beStateAddPassive.beStateAddBefore(passiveState, owner, state, time, preValidTime, this, damageReport);
            if (passiveState.shouldRemove(time)) {
                owner.removePassive(passiveState);
            }
        }
        return preValidTime;
    }

    public void executeStateAddPassiveAfter(Unit owner, Unit target, UnitState state, int time, int validTime, ITimedDamageReport damageReport) {
        List<PassiveState> passiveStates = owner.getStateAddPassive();
        if (passiveStates == null || passiveStates.isEmpty()) {
            return;
        }

        PassiveState[] array = passiveStates.toArray(new PassiveState[0]);
        for (PassiveState passiveState : array) {
            if (passiveState.invalid(time)) {
                continue;
            }
            if (passiveState.shouldRemove(time)) {
                owner.removePassive(passiveState);
                continue;
            }
            PassiveType passiveType = passiveState.getType();
            StateAddPassive beStateAddPassive = PassiveFactory.getPassive(passiveType);
            // 执行被动
            beStateAddPassive.stateAddAfter(passiveState, owner, target, state, time, validTime, this, damageReport);
            if (passiveState.shouldRemove(time)) {
                owner.removePassive(passiveState);
            }
        }
    }

    public long getOriginHp(Unit target) {
        AlterValue alterValue = origin.get(target);
        if (alterValue == null) {
            return 0;
        }
        return alterValue.getLongValue(AlterType.HP);
    }

    public boolean isCrit(Unit unit) {
        if (damageTags == null) {
            return false;
        }
        TargetDamageTag targetDamageTag = damageTags.get(unit);
        if (targetDamageTag == null) {
            return false;
        }
        return targetDamageTag.isCrit();
    }

    public void setCrit(Unit target, boolean isCrit) {
        if (!isCrit) {
            return;
        }
        if (damageTags == null) {
            damageTags = new HashMap<>(6);
        }
        TargetDamageTag targetDamageTag = damageTags.computeIfAbsent(target, k -> new TargetDamageTag());
        targetDamageTag.setCrit(true);
    }

    public boolean isMiss(Unit unit) {
        if (damageTags == null) {
            return false;
        }
        TargetDamageTag targetDamageTag = damageTags.get(unit);
        if (targetDamageTag == null) {
            return false;
        }
        return targetDamageTag.isMiss();
    }

    public void setMiss(Unit target, boolean isMiss) {
        if (!isMiss) {
            return;
        }
        if (damageTags == null) {
            damageTags = new HashMap<>(6);
        }
        TargetDamageTag targetDamageTag = damageTags.computeIfAbsent(target, k -> new TargetDamageTag());
        targetDamageTag.setMiss(true);
    }

    public boolean hasTag(Unit unit, String tag) {
        if (damageTags == null) {
            return false;
        }
        TargetDamageTag targetDamageTag = damageTags.get(unit);
        if (targetDamageTag == null) {
            return false;
        }
        return targetDamageTag.hasTag(tag);
    }

    public void addTag(Unit target, String tag) {
        if (damageTags == null) {
            damageTags = new HashMap<>();
        }
        TargetDamageTag targetDamageTag = damageTags.computeIfAbsent(target, k -> new TargetDamageTag());
        targetDamageTag.addTag(tag);
    }

    /**
     * @param caster
     * @param target    被添加异常的目标
     * @param type      异常类型
     * @param validTime 异常过期时刻
     * @return 异常过期时刻
     */
    public int addState(Unit caster, Unit target, UnitState type, int time, int validTime, ITimedDamageReport damageReport) {
        //健壮性鉴定
        if (type == null) {
            return -1;
        }
        //被添加异常状态前被动
        validTime = executeBeStateAddPassiveBefore(target, type, time, validTime, damageReport);
        //免疫鉴定
        final boolean immune = target.hasStateImmune(type, time);
        if (immune) {
            return -1;
        }
        //控制状态成功率鉴定
        if (type.controlState()) {
            if (!RandomUtils.isHit(1 - target.getRate(UnitRate.CONTROL_STATE_IMMUNE_RATE))) {
                return -1;
            }
        }


        if (unitStates == null) {
            unitStates = new HashMap<>(6);
        }

        //计算实际过期时刻
        final int battleTime = attacker.getBattle().getBattleTime();
        validTime = stateValidTimeModifier(type, validTime, battleTime, target);

        if (validTime < time) {
            return -1;
        }

        UnitStateContext unitStateContext = unitStates.computeIfAbsent(target, k -> new UnitStateContext());
        unitStateContext.add(type, validTime);

        executeStateAddPassiveAfter(caster, target, type, time, validTime, damageReport);
        //行至此处异常已添加成功，其他被动若要在此后添加霸体或解除异常，须在被动内部自行实现驱散并添加相应战报
        return validTime;
    }


    public boolean hasHarmState(Unit unit) {
        if (unitStates == null) {
            return false;
        }
        UnitStateContext unitStateContext = unitStates.get(unit);
        if (unitStateContext == null) {
            return false;
        }
        return unitStateContext.hasHarm();
    }

    //  是否即将被控制
    public boolean toBeControlled(Unit unit) {
        if (unitStates == null) {
            return false;
        }
        UnitStateContext unitStateContext = unitStates.get(unit);
        if (unitStateContext == null) {
            return false;
        }
        return unitStateContext.toBeControlled();
    }


    public void clearHarmState(Unit unit) {
        if (unitStates == null) {
            return;
        }
        UnitStateContext unitStateContext = unitStates.get(unit);
        if (unitStateContext == null) {
            return;
        }
        unitStateContext.clearHarm();
    }

    public void decontrol(Unit unit) {
        if (unitStates == null) {
            return;
        }
        UnitStateContext unitStateContext = unitStates.get(unit);
        if (unitStateContext == null) {
            return;
        }
        unitStateContext.decontrol();
    }

    public void setMagic(Unit target) {
        if (damageTags == null) {
            damageTags = new HashMap<>(6);
        }
        TargetDamageTag targetDamageTag = damageTags.computeIfAbsent(target, k -> new TargetDamageTag());
        targetDamageTag.setMagic(true);
    }

    public boolean isMagic(Unit unit) {
        if (damageTags == null) {
            return false;
        }
        TargetDamageTag targetDamageTag = damageTags.get(unit);
        if (targetDamageTag == null) {
            return false;
        }
        return targetDamageTag.isMagic();
    }

    public void clearHpChange(Unit unit) {
        origin.remove(unit);
        passiveValues.remove(unit);
    }

    public void passiveRecover(Unit owner, Unit target, long recover, int time, PassiveState passiveState, ITimedDamageReport dmgReport) {
        addPassiveValue(target, AlterType.HP, recover);
        final Hp hp = Hp.fromRecover(recover);
        dmgReport.add(time, target.getId(), PassiveValue.single(passiveState.getId(), owner.getId(), hp));
    }

    public void setExecPassive(boolean execPassive) {
        this.execPassive = execPassive;
    }

    public <T> T getAddition(String additionName, T defaultObj) {
        if (addition == null) {
            addition = new HashMap<>(1);
        }
        final Object o = addition.computeIfAbsent(additionName, k -> defaultObj);
        return (T) o;
    }

    public void setAddition(String additionName, Object obj) {
        if (addition == null) {
            addition = new HashMap<>(1);
        }
        addition.put(additionName, obj);
    }

    public void modVal(Unit owner, Unit target, int time, ITimedDamageReport report, DefaultAddValueParam valModParam, String passiveId, Unit subject) {
        if (valModParam == null) {
            return;
        }
        if (subject == null) {
            subject = owner;
        }
        final CalValues calValues = CalTypeHelper.calValues(valModParam.getCalType(), valModParam.getAlters(), subject, target, 0);
        final String targetId = target.getId();
        if (StringUtils.isEmpty(passiveId)) {
            for (Map.Entry<AlterType, Number> entry : calValues.getValues().entrySet()) {
                AlterType alterType = entry.getKey();
                Number number = entry.getValue();
                report.add(time, targetId, new UnitValues(alterType, number));
                addValue(target, alterType, number);
            }
        } else {
            final PassiveValue passiveValue = PassiveValue.of(passiveId, owner.getId());
            for (Map.Entry<AlterType, Number> entry : calValues.getValues().entrySet()) {
                AlterType alterType = entry.getKey();
                Number number = entry.getValue();
                passiveValue.add(new UnitValues(alterType, number));
                addPassiveValue(target, alterType, number);
            }
            report.add(time, targetId, passiveValue);
        }
    }

    public void passiveAtkDmg(Unit owner, Unit target, int time, ITimedDamageReport report, HpHigherDamage higherDamage, DamageParam dmgParam, String passiveId, Unit subject) {
        if (dmgParam == null) {
            return;
        }

        if (subject == null) {
            subject = owner;
        }
        final HpHigherDamage.HigherDmgResult res = higherDamage.calcHigherDmgRes(subject, target, null, dmgParam, time);
        final long damage = res.getDamage();
        addPassiveValue(target, AlterType.HP, damage);
        setCrit(target, res.isCrit());
        if (res.isMagic()) {
            setMagic(target);
        }
        report.add(time, target.getId(), PassiveValue.single(passiveId, owner.getId(), Hp.of(damage)));
    }

    public void passiveRecover(Unit owner, Unit target, int time, HpRecover recover, HpRecoverParam param, PassiveState passiveState, ITimedDamageReport dmgReport, Unit subject) {
        if (param == null) {
            return;
        }
        if (subject == null) {
            subject = owner;
        }
        final HpRecover.RecoverResult res = recover.calcRecoverRes(subject, target, param);
        passiveRecover(owner, target, res.getRecover(), time, passiveState, dmgReport);
    }
}
