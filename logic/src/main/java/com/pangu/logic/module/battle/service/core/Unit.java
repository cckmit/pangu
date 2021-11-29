package com.pangu.logic.module.battle.service.core;

import com.pangu.logic.module.battle.model.*;
import com.pangu.logic.module.battle.model.report.values.ItemAdd;
import com.pangu.logic.module.battle.model.report.values.ItemMove;
import com.pangu.logic.module.battle.service.Battle;
import com.pangu.logic.module.battle.service.BattleConstant;
import com.pangu.logic.module.battle.service.action.*;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.DispelType;
import com.pangu.logic.module.battle.service.passive.PassiveFactory;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.Phase;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 战斗单元<br/>
 * 该对象用于表示战斗中的计算个体
 */
@Getter
@Setter
public class Unit implements Comparable<Unit> {

    //  攻击方标识前缀
    public static final String ATTACKER_PREFIX = "A";
    //  防守方标识前缀
    public static final String DEFENDER_PREFIX = "D";

    // 实例域定义部分

    //  标识
    private String id;
    //  等级
    private int level;
    // 站位顺序
    private int sequence;
    //  坐标(格子)
    private Point point;
    //  显示信息
    private ModelInfo model;
    // 战力
    private long fight;
    //  数值属性
    private HashMap<UnitValue, Long> values = new HashMap<>(UnitValue.values().length);
    // 初始数值属性
    private HashMap<UnitValue, Long> originValues = new HashMap<>(UnitValue.values().length);

    //  比率属性(累加关系)
    private HashMap<UnitRate, Double> rates = new HashMap<>();
    //  状态对应有效时间 按照UnitState枚举值下标
    private int[] stateValidTime = new int[UnitState.values().length];

    // 死亡
    private boolean dead;
    // 是否可复活
    private boolean revivable = true;

    // 死亡被动是否触发过
    private boolean diePassiveTriggered;

    // 羁绊技能
    private List<SkillState> fatterSkills;
    // 初始技能
    private List<SkillState> initSkills;
    // 由被动触发释放的技能
    private List<SkillState> passiveSkills;
    //  技能状态,技能以及大招
    private List<SkillState> activeSkills;
    // 普攻
    private SkillState normalSkill;

    // 按照阶段分类的被动
    private Map<Phase, List<PassiveState>> passiveStates = new HashMap<>(Phase.values().length);
    private Map<String, PassiveState> passiveStatesById = new HashMap<>(6);

    // buff驱散类型buff
    private Map<DispelType, List<BuffState>> dispelBuffStates = new HashMap<>();
    // 根据tag标识buff
    private Map<String, BuffState> tagBuffStates = new HashMap<>();

    // 移动时距离目标多远停下
    private int minMoveDistance;

    // 攻击目标
    private Unit target;

    // 需要追踪的单元
    private Unit traceUnit;

    // 召唤者
    private Unit summonUnit;

    // 召唤单元
    private boolean summon;

    // 是否加入fighter
    private boolean joinFighter;

    // 是否通过Battle对象构建，为true意味着在战场初始化时就存在的单元
    private boolean initByBattle;

    // 上次受伤百分比增加怒气值
    private long lastDamageMpPercent;

    // 飞镖信息，只有特定英雄才有飞镖信息
    private LinkedList<ItemAdd> items;

    //  友方
    private Fighter friend;
    //  敌方
    private Fighter enemy;


    // 技能效果行动队列
    private PriorityQueue<Action> timeActions = new PriorityQueue<>(3);

    // 技能行动(修改以及延长行动时间)
    private Action action;

    // 最近的行动时间
    private int minActionTime;

    // 没有有效行动时间
    private boolean noActionTime;

    // 变身状态码。0为初始状态
    private int transformState;

    public static String toUnitId(boolean isAttacker, int sequence) {
        if (isAttacker) {
            return ATTACKER_PREFIX + ":" + sequence;
        } else {
            return DEFENDER_PREFIX + ":" + sequence;
        }
    }

    public static boolean canBeSelect(Unit unit, int time) {
        return unit.canSelect(time);
    }

    public boolean canFightBack(int time) {
        return !isDead()
                && !hasState(UnitState.CHAOS, time)
                && !hasState(UnitState.DISABLE, time)
                && !hasState(UnitState.EXILE, time);
    }

    @Override
    public String toString() {
        return "[id=" + id + ", name=" + model.getName() + "]";
    }

    //  构造方法
    public static Unit valueOf(ModelInfo model, HashMap<UnitValue, Long> values,
                               HashMap<UnitRate, Double> rates, int state, String[] skills, String[] passives) {

        List<SkillState> skillStates = Collections.emptyList();
        if (skills != null && skills.length > 0) {
            skillStates = new ArrayList<>(skills.length);
            for (String skillId : skills) {
                SkillState initState = SkillFactory.initState(skillId);
                skillStates.add(initState);
            }
        }
        List<PassiveState> passiveStates = Collections.emptyList();
        if (passives != null && passives.length != 0) {
            passiveStates = new ArrayList<>(passives.length);
            for (String passiveId : passives) {
                PassiveState passiveState = PassiveFactory.initState(passiveId, 0);
                passiveStates.add(passiveState);
            }
        }
        return valueOf(model, values, rates, state, skillStates, passiveStates);
    }

    //  构造方法
    public static Unit valueOf(ModelInfo model, HashMap<UnitValue, Long> values,
                               HashMap<UnitRate, Double> rates, int state, List<SkillState> skills,
                               List<PassiveState> passiveStates) {
        Unit result = new Unit();
        result.model = model;
        if (values != null) {
            result.values.putAll(values);
        }
        if (rates != null) {
            result.rates.putAll(rates);
        }

        result.originValues.putAll(result.values);

        int skillSize = skills.size();
        // 普攻
        SkillState normalSkill = null;
        // 主动技能及大招
        List<SkillState> activeSkills = new ArrayList<>(skillSize);
        // 羁绊技能
        List<SkillState> fatterSkills = new ArrayList<>(skillSize);
        // 初始化技能
        List<SkillState> initSkills = new ArrayList<>(skillSize);
        int minMoveDistance = Integer.MAX_VALUE;
        for (SkillState item : skills) {
            SkillType type = item.getType();
            switch (type) {
                case NORMAL:
                    normalSkill = item;
                    if (item.getRange() > 0) {
                        minMoveDistance = Integer.min(minMoveDistance, item.getRange());
                    }
                    break;
                case SKILL:
                case SPACE:
                    activeSkills.add(item);
//                    if (item.getRange() > 0) {
//                        minMoveDistance = Integer.min(minMoveDistance, item.getRange());
//                    }
                    break;
                case INIT:
                    initSkills.add(item);
                    break;
                case FATTER:
                    fatterSkills.add(item);
            }
        }
        if (minMoveDistance == Integer.MAX_VALUE) {
            minMoveDistance = 0;
        }
        // 排序
        activeSkills.sort(BattleConstant.SKILL_PRIORITY);
        fatterSkills.sort(BattleConstant.SPECIAL_SKILL_PRIORITY);
        initSkills.sort(BattleConstant.SPECIAL_SKILL_PRIORITY);

        result.normalSkill = normalSkill;
        result.activeSkills = activeSkills;
        result.fatterSkills = fatterSkills;
        result.initSkills = initSkills;

        for (PassiveState passiveState : passiveStates) {
            result.addPassive(passiveState, null);

            passiveState.setCaster(result);
            passiveState.setOwner(result);
        }

        result.minMoveDistance = minMoveDistance;
        return result;
    }

    public Battle getBattle() {
        return friend.getBattle();
    }

    public void setValue(UnitValue unitValue, long value) {
        this.values.put(unitValue, value);
    }

    /**
     * 增加/减少属性值(累加关系)
     *
     * @param type  约定的键名
     * @param value 增量
     * @return 最新值
     */
    public long increaseValue(UnitValue type, long value) {
        long current = getValue(type);
        switch (type) {
            case HP:
                current += value;
                if (current <= 0) {
                    current = 0;
                } else if (current > getValue(UnitValue.HP_MAX)) {
                    current = getValue(UnitValue.HP_MAX);
                }
                setValue(type, current);
                break;
            case MP:
                current += value;
                if (current <= 0) {
                    current = 0;
                } else if (current > getValue(UnitValue.MP_MAX)) {
                    current = getValue(UnitValue.MP_MAX);
                }
                setValue(type, current);
                break;
            case EP:
                current += value;
                if (current <= 0) {
                    current = 0;
                } else if (current > getValue(UnitValue.EP_MAX)) {
                    current = getValue(UnitValue.EP_MAX);
                }
                setValue(type, current);
                break;
            default:
                current += value;
                if (current < 0) {
                    current = 0;
                }
                setValue(type, current);
                break;
        }
        return current;
    }

    public long getOriginValue(String type) {
        return getOriginValue(UnitValue.valueOf(type));
    }

    public long getOriginValue(UnitValue type) {
        return originValues.getOrDefault(type, 0L);
    }

    public long getValue(String type) {
        return values.getOrDefault(UnitValue.valueOf(type), 0L);
    }

    public long getValue(UnitValue type) {
        return values.getOrDefault(type, 0L);
    }


    /**
     * 增加/减少比率值(累加关系)
     *
     * @param rate  约定的键名
     * @param value 增量
     * @return 最新值
     */
    public double increaseRate(UnitRate rate, double value) {
        double current = getRate(rate);
        current += value;
//        if (current < 0) {
//            current = 0.0D;
//        }
        setRate(rate, current);
        return current;
    }

    /**
     * 设置指定比率(累加关系)
     *
     * @param rate  比率
     * @param value 值
     * @return
     */
    public void setRate(UnitRate rate, double value) {
        rates.put(rate, value);
    }

    /**
     * 获取指定比率(累加关系)
     *
     * @param type 类型
     * @return
     */
    public double getRate(UnitRate type) {
        return rates.getOrDefault(type, 0.0D);
    }

    /**
     * 获取指定比率(累加关系)
     *
     * @param type 类型
     * @return
     */
    public double getRate(String type) {
        return getRate(UnitRate.valueOf(type));
    }

    public SkillState takeFatterSkill() {
        int size = fatterSkills.size();
        if (size == 0) {
            return null;
        }
        return fatterSkills.remove(size - 1);
    }

    public SkillState takeInitSkill() {
        int size = initSkills.size();
        if (size == 0) {
            return null;
        }
        return initSkills.remove(size - 1);
    }

    public boolean removeInitSkill(String skillId) {
        final Iterator<SkillState> it = initSkills.iterator();
        while (it.hasNext()) {
            final SkillState skillState = it.next();
            if (skillState.getId().equals(skillId)) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    public void reset(int time) {
        this.action = new BeginAction(time, this);
        if (timeActions.size() == 0) {
            this.minActionTime = time;
        } else {
            this.minActionTime = Math.min(time, timeActions.peek().getTime());
        }
    }

    public void updateAction(Action action) {
        this.action = action;
        int time = action.getTime();
        if (timeActions.size() == 0) {
            this.minActionTime = time;
        } else {
            this.minActionTime = Math.min(time, timeActions.peek().getTime());
        }
    }

    public void addTimedAction(Action action) {
        timeActions.offer(action);
        int peekTime = timeActions.peek().getTime();
        if (this.action == null) {
            this.minActionTime = peekTime;
        } else {
            this.minActionTime = Math.min(peekTime, this.action.getTime());
        }
    }

    public Action takeNextAction() {
        int actionTime = this.action == null ? Integer.MAX_VALUE : this.action.getTime();
        Action effectAction = null;

        boolean containInvalid = false;
        while (timeActions.size() != 0) {
            Action item = timeActions.peek();
            if (item instanceof CloseableAction && ((CloseableAction) item).isDone()) {
                timeActions.poll();
                containInvalid = true;
                continue;
            }
            effectAction = item;
            break;
        }
        if (containInvalid) {
            if (timeActions.isEmpty()) {
                this.minActionTime = this.action.getTime();
            } else {
                this.minActionTime = Math.min(this.action.getTime(), timeActions.peek().getTime());
            }
            return null;
        }
        if (effectAction != null && effectAction.getTime() <= actionTime) {
            timeActions.poll();
            if (timeActions.isEmpty()) {
                this.minActionTime = this.action.getTime();
            } else {
                Action peek = timeActions.peek();
                this.minActionTime = Math.min(peek.getTime(), this.action.getTime());
            }
            return effectAction;
        } else {
            if (this.action == null) {
                this.minActionTime = Integer.MAX_VALUE;
                return action;
            }
            if (timeActions.isEmpty()) {
                this.minActionTime = Integer.MAX_VALUE;
            } else {
                Action peek = timeActions.peek();
                this.minActionTime = peek.getTime();
            }
            Action pre = this.action;
            this.action = null;
            return pre;
        }
    }

    public boolean hasState(UnitState state, int time) {
        int validTime = stateValidTime[state.ordinal()];
        return validTime >= time && validTime > 0;
    }

    public boolean hasState(String state, int time) {
        return hasState(UnitState.valueOf(state), time);
    }

    public boolean immuneControl(int time) {
        return hasState(UnitState.BA_TI, time);
    }

    public boolean hasStateImmune(UnitState type, int time) {
        UnitState[] immune = type.immune;
        if (immune == null || immune.length == 0) {
            return false;
        }
        for (UnitState immu : immune) {
            if (hasState(immu, time)) {
                return true;
            }
        }
        return false;
    }

    public int getNoMoveTime() {
        return stateValidTime[UnitState.DISABLE.ordinal()];
    }

    public boolean isDead() {
        return dead;
    }

    public void move(Point point) {
        int x = point.x;
        if (x < 0) {
            x = 0;
        } else if (x > BattleConstant.MAX_X) {
            x = BattleConstant.MAX_X;
        }
        int y = point.y;
        if (y < 0) {
            y = 0;
        } else if (y > BattleConstant.MAX_Y) {
            y = BattleConstant.MAX_Y;
        }

        this.point.move(x, y);
    }

    //战斗计算公式中需要使用
    public int getFriendRaceSize(String raceType) {
        int size = 0;
        final HeroRaceType heroRaceType = HeroRaceType.valueOf(raceType);
        for (Unit unit : getFriend().getCurrent()) {
            final ModelInfo model = unit.getModel();
            if (model == null) {
                continue;
            }
            if (model.getRaceType() == heroRaceType) {
                size++;
            }
        }
        return size;
    }

    //获取对指定种族的伤害加深（战斗公式中需要）
    public double getDeepen(HeroRaceType type) {
        if (type == null) {
            return 0D;
        }
        final UnitRate deepen = type.getDeepen();
        if (deepen == null) {
            return 0D;
        }
        return rates.getOrDefault(deepen, 0D) * (1 + rates.getOrDefault(UnitRate.RACE_DEEPEN_ADD, 0D));
    }

    //获取单元指定种族（战斗公式中需要）
    public HeroRaceType getHeroRaceType() {
        return model.getRaceType();
    }

    public boolean isHeroRaceType(int type) {
        return model.getRaceType().ordinal() == type;
    }

    //获取单元性别
    public boolean isFemale() {
        return model.isFemale();
    }

    //获取对指定性别的伤害加深率
    public double getDeepen(boolean female) {
        UnitRate unitRate;
        if (female) {
            unitRate = UnitRate.FEMALE_DEEPEN;
        } else {
            unitRate = UnitRate.MALE_DEEPEN;
        }
        return rates.getOrDefault(unitRate, 0D);
    }

    //是否克制目标
    public boolean counter(Unit target) {
        final HeroRaceType targetRaceType = target.getHeroRaceType();
        return targetRaceType != null && this.rates.getOrDefault(targetRaceType.getDeepen(), 0D) > 0;
    }

    //是否被目标克制
    public boolean beCounter(Unit target) {
        return target.counter(this);
    }

    //目标是否为友方
    public boolean isFriend(Unit target) {
        return target.getFriend() == this.getFriend();
    }

    //对单元造成位移效果的技能一律应当调用该重载方法进行操作
    public void move(Point point, int time, UnitState... unitStates) {
        if (hasState(UnitState.NO_MOVE, time)) return;
        if (Arrays.stream(unitStates).anyMatch(state -> hasState(state, time))) return;
        move(point);
    }

    public HeroJobType getJob() {
        return model.getJob();
    }

    public boolean isJob(int ordinal) {
        return getJob().ordinal() == ordinal;
    }

    @Override
    public int compareTo(Unit o) {
        return Integer.compare(minActionTime, o.minActionTime);
    }

    public void dead() {
        dead = true;
        friend.die(this);
    }

    public void foreverDead() {
        dead();
        revivable = false;
    }

    private void revive() {
        dead = false;
        diePassiveTriggered = false;
        friend.revive(this);
    }

    public boolean revive(int time) {
        if (!revivable) {
            return false;
        }

        revive();
        //清除状态
        removeAllStates();
        //基本行为清理
        this.reset(time);
        //技能效果队列清理
        final PriorityQueue<Action> timeActions = this.getTimeActions();
        //  对于不同类型的action做不同类型的清理
        final Map<Boolean, List<Action>> isBuffAction = timeActions.stream()
                .collect(Collectors.groupingBy(timeAction -> timeAction instanceof BuffAction));
        //  BUFF清理：
        //    更新未过期的FIXED_BUFF的执行时间
        final List<Action> buffActions = isBuffAction.get(true);
        if (!CollectionUtils.isEmpty(buffActions)) {
            final Map<Boolean, List<BuffAction>> isUpdatableBuff = buffActions.stream()
                    .map(act -> (BuffAction) act)
                    .collect(Collectors.groupingBy(act -> act.getRemoveTime() > time && act.getBuffState().getDispelType() == DispelType.FIXED));
            final List<BuffAction> updatableBuffs = isUpdatableBuff.get(true);
            if (!CollectionUtils.isEmpty(updatableBuffs)) {
                for (BuffAction updatableBuff : updatableBuffs) {
                    updatableBuff.setTime(time);
                    this.timeActUpdate(updatableBuff);
                }
            }
            //    其余BUFF即刻移除
            final List<BuffAction> buffsToRemove = isUpdatableBuff.get(false);
            if (!CollectionUtils.isEmpty(buffsToRemove)) {
                for (BuffAction buffToRemove : buffsToRemove) {
                    BuffFactory.removeBuffState(buffToRemove.getBuffState(), this, time);
                }
            }
        }

        //  其他行动清理：清理一切执行时间过期的其他行为
        final List<Action> otherActions = isBuffAction.get(false);
        if (!CollectionUtils.isEmpty(otherActions)) {
            this.timeActions.removeAll(otherActions);
        }
        //将已死单位重新添加到角色队列里
        this.getBattle().addUnit(this);

        return true;
    }

    public void addPassive(String passiveId, int time, Unit caster) {
        final PassiveState passiveState = PassiveFactory.initState(passiveId, time);
        addPassive(passiveState, caster);
    }

    public void addPassive(PassiveState passiveState, Unit caster) {
        // 删除之前的被动
        removePassive(passiveState);

        passiveState.setCaster(caster);
        passiveState.setOwner(this);

        String passiveId = passiveState.getId();
        PassiveType type = passiveState.getType();
        Phase[] phases = type.getPhases();
        passiveStatesById.put(passiveId, passiveState);
        for (Phase phase : phases) {
            List<PassiveState> passiveStates = this.passiveStates.computeIfAbsent(phase, k -> new ArrayList<>(6));
            passiveStates.add(passiveState);
            passiveStates.sort(Comparator.comparingInt(a -> a.getType().getPriority()));
        }
        if (friend != null) {
            friend.addListener(this, phases);
        }
    }

    public List<PassiveState> getDamagePassive() {
        return passiveStates.getOrDefault(Phase.DAMAGE, Collections.emptyList());
    }

    public List<PassiveState> getAttackPassive() {
        return passiveStates.getOrDefault(Phase.ATTACK, Collections.emptyList());
    }

    public List<PassiveState> getAttackEndPassive() {
        return passiveStates.getOrDefault(Phase.ATTACK_END, Collections.emptyList());
    }

    public List<PassiveState> getRecoverPassive() {
        return passiveStates.getOrDefault(Phase.RECOVER, Collections.emptyList());
    }

    public List<PassiveState> getRecoverTargetPassive() {
        return passiveStates.getOrDefault(Phase.RECOVER_TARGET, Collections.emptyList());
    }

    public boolean removePassive(String passiveId) {
        final PassiveState passiveState = passiveStatesById.get(passiveId);
        if (passiveState == null) {
            return false;
        }
        removePassive(passiveState);
        return true;
    }

    public void removePassive(PassiveState passiveState) {
        String passiveId = passiveState.getId();
        PassiveState remove = passiveStatesById.remove(passiveId);
        if (remove == null) {
            return;
        }
        PassiveType type = passiveState.getType();
        Phase[] phases = type.getPhases();
        for (Phase phase : phases) {
            List<PassiveState> passiveStates = this.passiveStates.get(phase);
            passiveStates.remove(remove);
            if (passiveStates.isEmpty()) {
                this.passiveStates.remove(phase);
            }
        }
    }

    public BuffState getBuffStateByTag(String tag) {
        return tagBuffStates.get(tag);
    }

    /**
     * 添加一个无限有效的时间
     *
     * @param state
     */
    public void addState(UnitState state) {
        int ordinal = state.ordinal();
        stateValidTime[ordinal] = Integer.MAX_VALUE;
    }

    public void addState(UnitState state, int validTime) {
        int ordinal = state.ordinal();
        if (stateValidTime[ordinal] > validTime) {
            return;
        }
        stateValidTime[ordinal] = validTime;
    }

    public void removeState(UnitState unitState) {
        int ordinal = unitState.ordinal();
        stateValidTime[ordinal] = 0;
    }

    public boolean removeState(UnitState state, int validTime) {
        int ordinal = state.ordinal();
        if (stateValidTime[ordinal] > validTime) {
            return false;
        }
        stateValidTime[ordinal] = 0;
        return true;
    }

    public void removeStateByIfHarmful(boolean ifHarmful) {
        for (UnitState unitState : UnitState.values()) {
            if (unitState.harm == ifHarmful) {
                stateValidTime[unitState.ordinal()] = 0;
            }
        }
    }

    public void decontrol() {
        for (UnitState unitState : UnitState.values()) {
            if (unitState.controlState()) {
                stateValidTime[unitState.ordinal()] = 0;
            }
        }
    }

    private void removeAllStates() {
        Arrays.fill(stateValidTime, 0);
    }

    public List<BuffState> getBuffByDispel(DispelType dispelType) {
        return dispelBuffStates.get(dispelType);
    }

    public List<BuffState> getBuffBySettingId(String buffId) {
        List<BuffState> result = new LinkedList<>();
        for (BuffState buffState : tagBuffStates.values()) {
            if (buffState.getId().equals(buffId)) {
                result.add(buffState);
            }
        }
        return result;
    }

    public List<BuffState> getBuffByClassify(String classify) {
        List<BuffState> result = new LinkedList<>();
        for (BuffState buffState : tagBuffStates.values()) {
            if (buffState.hasClassify(classify)) {
                result.add(buffState);
            }
        }
        return result;
    }

    public PassiveState getPassiveStates(String passiveId) {
        return passiveStatesById.get(passiveId);
    }

    public List<PassiveState> getPassiveStateByType(PassiveType passiveType) {
        final Map<PassiveType, List<PassiveState>> passiveStatesByType = passiveStatesById.values().stream().collect(Collectors.groupingBy(PassiveState::getType));
        return passiveStatesByType.getOrDefault(passiveType, Collections.emptyList());
    }


    public boolean removeBuff(BuffState state) {
        String tag = state.getTag();
        boolean remove = tagBuffStates.remove(tag, state);
        if (!remove) {
            return false;
        }

        List<BuffState> buffStates = dispelBuffStates.get(state.getDispelType());
        buffStates.remove(state);
        return true;
    }

    public List<ItemAdd> addItem(int time, Point[] points) {
        return addItem(time, points, 20_000);
    }

    public List<ItemAdd> addItem(int time, Point[] points, int duration) {
        if (items == null) {
            this.items = new LinkedList<>();
        }
        List<ItemAdd> createItems = new ArrayList<>(points.length);
        Battle battle = friend.getBattle();
        for (Point point : points) {
            int knifeIndex = battle.itemIndex();
            ItemAdd knife = new ItemAdd(knifeIndex, point, time + duration);
            this.items.add(knife);
            createItems.add(knife);
        }
        // 删除过期飞镖
        while (!items.isEmpty()) {
            ItemAdd knife = items.getFirst();
            if (knife.getInvalidTime() >= time) {
                break;
            }
            items.removeFirst();
        }
        return createItems;
    }

    public List<ItemMove> moveItem(Map<ItemAdd, Point> itemDestination) {
        final ArrayList<ItemMove> itemMoves = new ArrayList<>();
        for (Map.Entry<ItemAdd, Point> entry : itemDestination.entrySet()) {
            final ItemAdd item = entry.getKey();
            item.getPoint().move(entry.getValue());
            itemMoves.add(new ItemMove(item.getId(), item.getPoint()));
        }
        return itemMoves;
    }

    public void removeItem(ItemAdd itemAdd) {
        items.remove(itemAdd);
    }

    public double getCurrentHpRate() {
        return getValue(UnitValue.HP) / 1D / getValue(UnitValue.HP_MAX);
    }

    public void addBuff(BuffState state) {
        String tag = state.getTag();
        BuffState pre = tagBuffStates.put(tag, state);
        if (pre != null) {
            List<BuffState> buffStates = dispelBuffStates.get(pre.getDispelType());
            if (buffStates != null) {
                buffStates.remove(pre);
            }
        }
        List<BuffState> buffStates = dispelBuffStates.computeIfAbsent(state.getDispelType(), k -> new ArrayList<>());
        buffStates.add(state);
    }

    public Collection<UnitType> getProfession() {
        return model.getProfessions();
    }

    public void addSkill(String skillId) {
        final SkillState skillState = SkillFactory.initState(skillId);
        switch (skillState.getType()) {
            case INIT:
            case FATTER:
                addInitEffect(skillState);
                break;
            case SKILL:
            case SPACE:
                addActiveSkill(skillState);
                break;
        }
    }

    public void addInitEffect(String skillId) {
        SkillState skillState = SkillFactory.initState(skillId);
        addInitEffect(skillState);
    }

    private void addInitEffect(SkillState skillState) {
        SkillType type = skillState.getType();
        if (type != SkillType.INIT && type != SkillType.FATTER) {
            throw new IllegalStateException("技能ID[" + skillState.getId() + "]类型不属于初始化技能和羁绊技能");
        }
        if (type == SkillType.INIT) {
            initSkills.add(skillState);
        } else {
            fatterSkills.add(skillState);
        }
    }

    public void addActiveSkill(SkillState skillState) {
        SkillType type = skillState.getType();
        if (type != SkillType.SPACE && type != SkillType.SKILL) {
            throw new IllegalStateException("技能ID[" + skillState.getId() + "]类型不属于普通技能和怒气技能");
        }
        activeSkills.add(skillState);
    }

    public List<PassiveState> getSkillSelectPassive() {
        return passiveStates.get(Phase.SKILL_SELECT);
    }

    public List<PassiveState> getHpChangePassives() {
        return passiveStates.get(Phase.HP_DOWN);
    }

    public List<PassiveState> getOwnerDiePassives() {
        return passiveStates.get(Phase.OWNER_DIE);
    }

    public List<PassiveState> getUnitDiePassives() {
        return passiveStates.get(Phase.DIE);
    }

    public List<PassiveState> getAttackBeforePassive() {
        return passiveStates.get(Phase.ATTACK_BEFORE);
    }

    public List<PassiveState> getBeAttackBeforePassive() {
        return passiveStates.get(Phase.BE_ATTACK_BEFORE);
    }

    public List<PassiveState> getBeStateAddPassive() {
        return passiveStates.get(Phase.BE_STATE_ADD);
    }

    public List<PassiveState> getStateAddPassive() {
        return passiveStates.get(Phase.STATE_ADD);
    }

    public List<PassiveState> getSkillPassive() {
        return passiveStates.get(Phase.SKILL_RELEASE);
    }

    public void timeActUpdate(Action action) {
        boolean remove = timeActions.remove(action);
        if (remove) {
            addTimedAction(action);
        }
    }

    public double getHpPct() {
        if (isDead()) {
            return 0;
        }
        return getValue(UnitValue.HP) / 1.0 / getValue(UnitValue.HP_MAX);
    }

    public double getMpPct() {
        return getValue(UnitValue.MP) / 1.0 / getValue(UnitValue.MP_MAX);
    }

    public long getHighestATK() {
        return Math.max(getValue(UnitValue.ATTACK_M), getValue(UnitValue.ATTACK_P));
    }


    public List<PassiveState> getInitPassives() {
        return passiveStates.get(Phase.INIT);
    }

    public List<UnitState> getStates(boolean harm, int time) {
        List<UnitState> result = new LinkedList<>();
        for (UnitState state : UnitState.values()) {
            if (state.harm == harm && hasState(state, time)) {
                result.add(state);
            }
        }
        return result;
    }

    public boolean inHarmfulState(int time) {
        for (UnitState state : UnitState.values()) {
            if (state.harm && hasState(state, time)) {
                return true;
            }
        }
        return false;
    }

    public boolean underControl(int time) {
        for (UnitState state : UnitState.CONTROL_STATE) {
            if (hasState(state, time)) {
                return true;
            }
        }
        return false;
    }

    public List<SkillState> getActiveSkillsByTag(String skillTag) {
        List<SkillState> result = new LinkedList<>();
        for (SkillState skillState : activeSkills) {
            if (skillState.getTag().equals(skillTag)) {
                result.add(skillState);
            }
        }
        return result;
    }

    public boolean canSelect(int time) {
        return !isDead()
                && !hasState(UnitState.UNVISUAL, time)
                && !hasState(UnitState.EXILE, time)
                && isJoinFighter();
    }

    public boolean hasClassifyBuff(String classify) {
        for (BuffState buffState : getTagBuffStates().values()) {
            if (buffState.hasClassify(classify)) {
                return true;
            }
        }
        return false;
    }

    public void upgradeActiveSkillByTag(String skillTag, int idx) {
        for (int i = 0; i < activeSkills.size(); i++) {
            final SkillState skillStateToUpgrade = activeSkills.get(i);
            if (!skillStateToUpgrade.getTag().equals(skillTag)) {
                continue;
            }

            final String[] upgrades = skillStateToUpgrade.getSetting().getUpgrade();
            if (ArrayUtils.isEmpty(upgrades)) {
                continue;
            }

            activeSkills.set(i, SkillFactory.initState(upgrades[idx]));
        }
    }

    public void upgradeNormalSkill(int idx) {
        final String[] upgrades = normalSkill.getSetting().getUpgrade();
        if (ArrayUtils.isEmpty(upgrades)) {
            return;
        }
        normalSkill = SkillFactory.initState(upgrades[idx]);
    }

    public int calcDistance(Unit target) {
        return point.distance(target.getPoint());
    }

    public boolean cancelAction(int time) {
        if (action instanceof MoveAction) {
            return true;
        }
        if (action instanceof SkillAction) {
            final SkillAction skillAction = (SkillAction) action;
            if (skillAction.getSkillState().getType() == SkillType.NORMAL) {
                skillAction.broken(time);
                return true;
            }
        }
        return false;
    }

    public boolean heroUnit() {
        return !isSummon() && isJoinFighter();
    }
}
