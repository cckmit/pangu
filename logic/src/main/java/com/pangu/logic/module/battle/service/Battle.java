package com.pangu.logic.module.battle.service;

import com.pangu.logic.module.battle.model.BattleResult;
import com.pangu.logic.module.battle.model.BattleType;
import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.model.UnitValue;
import com.pangu.logic.module.battle.model.report.FightReport;
import com.pangu.logic.module.battle.model.report.IReport;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.resource.BattleSetting;
import com.pangu.logic.module.battle.service.action.Action;
import com.pangu.logic.module.battle.service.action.IntervalAddMpAction;
import com.pangu.logic.module.battle.service.action.SkillAction;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.Fighter;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.InitPassive;
import com.pangu.logic.module.battle.service.passive.PassiveFactory;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.framework.utils.json.JsonUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 一个战场
 */
@Getter
@Slf4j
public class Battle {

    //  攻击方
    @Getter
    private Fighter attacker;

    //  防守方
    @Getter
    private Fighter defender;

    private String sceneId;

    // 场上显示道具ID
    private int itemIndex;

    // 全局buff索引
    private int buffIndex;

    //叠加BUFF标签
    private int customBuffIndex;

    // 战场配置信息
    @Getter
    private BattleSetting config;

    // 战斗类型
    @Getter
    private BattleType type;

    // 本场战报
    @Getter
    private FightReport report;

    // 战斗结果
    @Getter
    private BattleResult result;

    // 世界行动
    @Getter
    private final PriorityQueue<Action> worldActions = new PriorityQueue<>();

    // 英雄的行动队列
    private final PriorityQueue<Unit> actionUnits = new PriorityQueue<>(12);

    // 大招的cd时间
    private int spaceCD;

    // 大招就绪，而无法施放队列
    @Getter
    private ArrayDeque<Unit> spaceValidUnits = new ArrayDeque<>(6);

    // 记录战场时间，供各个单元直接访问
    private int battleTime;

    public static Battle valueOfNoDefender(Fighter attacker, BattleType type, String sceneId) {
        Battle battle = new Battle();
        battle.attacker = attacker == null ? Fighter.emptyOf() : attacker;
        battle.result = BattleResult.ATTACKER;
        battle.defender = Fighter.emptyOf();
        battle.report = FightReport.valueOfEmptyDefender(attacker, type, sceneId);
        return battle;
    }

    public static Battle valueOfNoAttacker(Fighter defender, BattleType type, String sceneId) {
        Battle battle = new Battle();
        battle.defender = defender == null ? Fighter.emptyOf() : defender;
        battle.attacker = Fighter.emptyOf();
        battle.result = BattleResult.DEFENDER;
        battle.report = FightReport.valueOfEmptyAttacker(defender, type, sceneId);
        return battle;
    }

    public static Battle valueOf(BattleType type, BattleSetting config, Fighter attacker, Fighter defender, String sceneId) {
        Battle battle = new Battle();
        battle.type = type;
        battle.attacker = attacker;
        battle.defender = defender;
        battle.config = config;
        battle.sceneId = sceneId;
        return battle;
    }

    /**
     * 开始战斗
     */
    public void start() {
        // 初始化站位等信息
        initPosition();
        // 执行所有单元羁绊技能和初始化技能
        executeFatterSkill();
        executeInitSkill();
        // 执行所有初始被动技能
        initPassive(attacker.getCurrent());
        initPassive(defender.getCurrent());
        // 最大行动时间
        int timeLimit = config.getTime();
        // 开启计时点
        long start = System.nanoTime();
        // 循环执行行动
        int time = 0;
        while (actionUnits.size() != 0) {
            Unit unit = actionUnits.peek();
            if (unit.isDead()) {
                Fighter friend = unit.getFriend();
                if (friend.isDie()) {
                    result = friend == attacker ? BattleResult.DEFENDER : BattleResult.ATTACKER;
                    break;
                }
                actionUnits.poll();
                continue;
            }
            int minActionTime = unit.getMinActionTime();

            Action worldAction = worldActions.peek();

            Action action;
            // 对比世界行动与玩家行动中较早的行动
            if (worldAction == null || worldAction.getTime() > minActionTime) {
                actionUnits.poll();
                if (unit.isDead()) {
                    Fighter friend = unit.getFriend();
                    if (friend.isDie()) {
                        result = friend == attacker ? BattleResult.DEFENDER : BattleResult.ATTACKER;
                        break;
                    }
                    continue;
                }
                action = unit.takeNextAction();
            } else {
                action = worldActions.poll();
                unit = null;
            }
            if (action == null) {
                if (unit != null && unit.getAction() != null) {
                    actionUnits.offer(unit);
                }
                continue;
            }
            time = action.getTime();
            battleTime = time;
            if (time >= timeLimit) {
                result = BattleResult.TIME_OUT;
                break;
            }
            long current = System.nanoTime();
            if (BattleConstant.BATTLE_DEADLINE && (current - start) > 1500_000_000 && BattleConstant.showReport()) {
                result = BattleResult.TIME_OUT;
                log.warn("战斗超过预期时间[{}]，直接终止，预防问题扩大[{}]", current - start, JsonUtils.object2String(report));
                break;
            }
            // 执行行动
            action.execute();

            if (unit != null) {
                if (unit.isDead()) {
                    Fighter enemy = unit.getEnemy();
                    if (enemy.isDie()) {
                        result = enemy == attacker ? BattleResult.DEFENDER : BattleResult.ATTACKER;
                        break;
                    }
                    Fighter friend = unit.getFriend();
                    if (friend.isDie()) {
                        result = friend == attacker ? BattleResult.DEFENDER : BattleResult.ATTACKER;
                        break;
                    }
                } else {
                    actionUnits.offer(unit);
                }
                continue;
            }
            if (attacker.isDie()) {
                result = BattleResult.DEFENDER;
                break;
            }
            if (defender.isDie()) {
                result = BattleResult.ATTACKER;
                break;
            }
        }
        if (result == null) {
            result = BattleResult.TIME_OUT;
        }
        report.setTime(time);
        if (result != BattleResult.ATTACKER && result != BattleResult.DEFENDER) {
            result = BattleResult.DEFENDER;
        }
        report.setResult(result);
    }

    private void executeInitSkill() {
        for (Unit unit : actionUnits.toArray(new Unit[0])) {
            if (unit.isDead()) {
                continue;
            }
            List<SkillState> initSkills = unit.getInitSkills();
            if (initSkills == null) {
                continue;
            }
            ArrayList<SkillState> skillStates = new ArrayList<>(initSkills);
            initSkills.clear();
            executeSkill(unit, skillStates, initSkills);
        }
    }

    private void executeFatterSkill() {
        for (Unit unit : actionUnits.toArray(new Unit[0])) {
            if (unit.isDead()) {
                continue;
            }
            List<SkillState> fatterSkills = unit.getFatterSkills();
            if (fatterSkills == null) {
                continue;
            }
            ArrayList<SkillState> skillStates = new ArrayList<>(fatterSkills);
            fatterSkills.clear();
            executeSkill(unit, skillStates, fatterSkills);
        }
    }

    private void executeSkill(Unit unit, ArrayList<SkillState> skillStates, List<SkillState> failRecover) {
        for (int i = skillStates.size() - 1; i >= 0; i--) {
            SkillState skillState = skillStates.get(i);
            if (skillState == null) {
                continue;
            }
            if (skillState.getSingTime() > 0 || skillState.getSingAfterDelay() > 0) {
                failRecover.add(skillState);
                continue;
            }
            SkillReport skillReport = SkillReport.sing(0, unit.getId(), skillState.getId(), 0, null);
            addReport(skillReport);

            SkillAction skillAction = new SkillAction(0, unit, skillState, skillReport, 0);
            unit.updateAction(skillAction);
            skillAction.execute();
        }

        if (failRecover.size() > 1) {
            Collections.reverse(failRecover);
        }
    }

    private void initPassive(Collection<Unit> units) {
        for (Unit unit : units) {
            List<PassiveState> passiveStates = unit.getInitPassives();
            if (passiveStates == null || passiveStates.size() == 0) {
                continue;
            }
            executeInitPassive(unit, passiveStates);
        }
    }

    private void executeInitPassive(Unit unit, List<PassiveState> passiveStates) {
        SkillReport skillReport = SkillReport.sing(0, unit.getId(), "_init_passive_skill_id", 0, null);
        Context context = new Context(unit);
        for (PassiveState passiveState : passiveStates) {
            PassiveType type = passiveState.getType();
            InitPassive passive = PassiveFactory.getPassive(type);
            passive.init(0, passiveState, unit, context, skillReport);
        }
        context.execute(0, skillReport);
    }

    private void initPosition() {
        attacker.setBattle(this);
        defender.setBattle(this);
        // 第一次增加怒气时间
        int addMpInterval = config.getAddMpInterval();
        addWorldAction(new IntervalAddMpAction(1 + addMpInterval, this));

        List<Unit> attackers = attacker.getCurrent();
        List<Unit> defenders = defender.getCurrent();
        if (attackers == null || attackers.isEmpty()) {
            throw new IllegalArgumentException("攻击方没有战斗单位，无法进入战斗...");
        }
        if (defenders == null || defenders.isEmpty()) {
            throw new IllegalArgumentException("防守方没有战斗单位，无法进入战斗...");
        }
        int[][] positions = config.getPositions();
        int maxX = BattleConstant.MAX_X;
        for (Unit unit : attackers) {
            int[] position = positions[unit.getSequence()];
            Point point = new Point(position[0], position[1]);
            initUnit(unit, point, attacker, defender);
            this.actionUnits.add(unit);
        }
        for (Unit unit : defenders) {
            int[] position = positions[unit.getSequence()];
            // 防御方X坐标相对于 中心轴位置，maxX/2 - x + maxX/2
            Point point = new Point(maxX - position[0], position[1]);
            initUnit(unit, point, defender, attacker);
            this.actionUnits.add(unit);
        }
        int initAddMp = config.getInitAddMp();
        if (initAddMp > 0) {
            for (Unit unit : this.actionUnits) {
                unit.increaseValue(UnitValue.MP, initAddMp);
            }
        }
        this.report = FightReport.valueOf(attacker, defender, type, sceneId);
    }

    private void initUnit(Unit unit, Point point, Fighter attacker, Fighter defender) {
        unit.setFriend(attacker);
        unit.setEnemy(defender);
        unit.setPoint(point);
        unit.setValue(UnitValue.MP_MAX, BattleConstant.MP_MAX);
        unit.setJoinFighter(true);
        unit.setInitByBattle(true);
        unit.reset(1);
    }

    public void addReport(IReport skillReport) {
        report.add(skillReport);
    }

    public void addWorldAction(Action action) {
        worldActions.offer(action);
    }

    public int itemIndex() {
        return ++itemIndex;
    }

    public String createBuffTag() {
        return String.valueOf(++customBuffIndex);
    }

    public int buffIndexIncr() {
        return ++buffIndex;
    }

    public void addUnit(Unit unit) {
        if (actionUnits.contains(unit)) {
            return;
        }
        this.actionUnits.add(unit);
    }

    public void unitActionUpdate(Unit unit) {
        boolean remove = actionUnits.remove(unit);
        if (remove && !unit.isDead()) {
            actionUnits.add(unit);
        }
    }

    public void worldActionUpdate(Action action) {
        boolean remove = worldActions.remove(action);
        if (remove) {
            worldActions.add(action);
        }
    }

    public boolean removeWorldAction(Action action) {
        return worldActions.remove(action);
    }

    public boolean isSpaceCD(int time) {
        return time < spaceCD;
    }

    public void addSpaceCD(int time) {
        this.spaceCD = time + config.getSpaceCD();
    }

    public boolean isPause() {
        return config.isPause();
    }

    public void addSpaceValidUnit(Unit unit) {
        if (spaceValidUnits.contains(unit)) {
            return;
        }
        spaceValidUnits.add(unit);
    }

    public void removeSpaceValidUnits(Unit unit) {
        spaceValidUnits.remove(unit);
    }
}
