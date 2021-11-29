package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.*;
import com.pangu.logic.module.battle.model.report.ITimedDamageReport;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.model.report.values.SummonRemove;
import com.pangu.logic.module.battle.model.report.values.SummonUnits;
import com.pangu.logic.module.battle.resource.EnemyUnitSetting;
import com.pangu.logic.module.battle.service.BattleConstant;
import com.pangu.logic.module.battle.service.EnemyUnitReader;
import com.pangu.logic.module.battle.service.alter.Alter;
import com.pangu.logic.module.battle.service.buff.utils.CalTypeHelper;
import com.pangu.logic.module.battle.service.buff.utils.CalValues;
import com.pangu.logic.module.battle.service.core.*;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.SummonSkillParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 召唤技能，召唤物往往附带易伤效果
 */
@Component
public class SummonSkill implements SkillEffect {
    @Autowired
    private EnemyUnitReader enemyStorage;

    @Override
    public EffectType getType() {
        return EffectType.SUMMON;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        doSummon(state, owner, time, context, skillReport, null);
    }

    public void doSummon(EffectState state, Unit owner, int time, Context context, ITimedDamageReport timedDamageReport, PassiveValue passiveValue) {
        SummonSkillParam param = state.getParam(SummonSkillParam.class);
        int times = param.getSummonAmount();
        boolean removePreUnit = param.isRemovePreSummon();

        if (removePreUnit) {
            @SuppressWarnings("unchecked")
            List<Unit> preSummons = state.getAddition(List.class);
            if (preSummons != null) {
                List<String> removeIds = new ArrayList<>(preSummons.size());
                for (Unit curUnit : preSummons) {
                    curUnit.dead();
                    removeIds.add(curUnit.getId());
                }

                if (passiveValue != null) {
                    passiveValue.add(new SummonRemove(removeIds));
                } else {
                    timedDamageReport.add(time, owner.getId(), new SummonRemove(removeIds));
                }
            }
        }
        List<Unit> summonUnits = new ArrayList<>(times);
        Unit refTarget;
        final String targetId = param.getTargetId();
        final List<Unit> refTargets = TargetSelector.select(owner, targetId, time);
        if (CollectionUtils.isEmpty(refTargets)) {
            refTarget = owner;
        } else {
            refTarget = refTargets.get(0);
        }
        for (int i = 0; i < times; ++i) {
            Unit unit = summonUnit(owner, param, refTarget, i, time);
            summonUnits.add(unit);
        }
        context.addSummon(summonUnits);

        final SummonUnits summonVal = new SummonUnits(summonUnits.stream().map(UnitInfo::valueOf).collect(Collectors.toList()));
        summonVal.setUnitType(param.getUnitType());
        if (passiveValue != null) {
            passiveValue.add(summonVal);
            timedDamageReport.add(time, owner.getId(), passiveValue);
        } else {
            timedDamageReport.add(time, owner.getId(), summonVal);
        }
        if (removePreUnit) {
            state.setAddition(summonUnits);
        }
    }

    /**
     * @param owner     召唤师单元
     * @param refTarget 召唤物面板属性的参照系单元
     * @param curIndex  一次召唤多个召唤物时，每个召唤物的在当次召唤时的召唤次序
     * @return 召唤物单元
     */
    public Unit summonUnit(Unit owner, SummonSkillParam param, Unit refTarget, int curIndex, int time) {
        Fighter friend = owner.getFriend();
        final EnemyUnitSetting setting = enemyStorage.get(param.getBaseId(), true);
        int index = friend.nextSummonIndex();
        String id = Unit.toUnitId(friend.isAttacker(), index);
        Unit unit = setting.toUnit(id, null);
        unit.setSequence(6);
        if (param.getRate() > 0) {
            buildUnitValuesByRate(unit, refTarget, param.getRate());
        } else {
            buildUnitValuesByCalType(unit, owner, refTarget, param);
        }
        unit.setSummon(param.isSummon());
        unit.setJoinFighter(param.isJoinFighter());
        unit.setSummonUnit(owner);
        final Point point = param.getPoint();
        if (point == null) {
            final List<Unit> candidates = TargetSelector.select(owner, param.getMomSelectId(), time);
            Unit elect = owner;
            if (!candidates.isEmpty()) {
                elect = candidates.get(0);
            }
            unit.setPoint(defaultSummonPoint(elect, curIndex, param.getDist(), param.getAngle()));
        } else {
            if (param.isReverse() && !friend.isAttacker()) {
                unit.setPoint(new Point(BattleConstant.MAX_X - point.getX(), point.getY()));
            } else {
                unit.setPoint(new Point(point));
            }
        }
        unit.setFriend(friend);
        unit.setEnemy(owner.getEnemy());
        return unit;
    }

    private void buildUnitValuesByRate(Unit unit, Unit refTarget, double rate) {
        final long hpMax = refTarget.getValue(UnitValue.HP_MAX);
        long hp = (long) (hpMax * rate);
        unit.getValues().put(UnitValue.HP, hp);
        unit.getValues().put(UnitValue.HP_MAX, hp);
        unit.getValues().put(UnitValue.ATTACK_M, (long) (refTarget.getValue(UnitValue.ATTACK_M) * rate));
        unit.getValues().put(UnitValue.ATTACK_P, (long) (refTarget.getValue(UnitValue.ATTACK_P) * rate));
        unit.getValues().put(UnitValue.DEFENCE_M, (long) (refTarget.getValue(UnitValue.DEFENCE_M) * rate));
        unit.getValues().put(UnitValue.DEFENCE_P, (long) (refTarget.getValue(UnitValue.DEFENCE_P) * rate));

        unit.setOriginValues(unit.getValues());
    }

    private void buildUnitValuesByCalType(Unit unit, Unit owner, Unit refTarget, SummonSkillParam param) {
        final CalValues calValues = CalTypeHelper.calValues(param.getCalType(), param.getAlters(), owner, refTarget, 0);
        for (Map.Entry<AlterType, Number> entry : calValues.getValues().entrySet()) {
            final AlterType key = entry.getKey();
            final Alter alter = key.getAlter();
            if (key == AlterType.HP) {
                unit.setValue(UnitValue.HP_MAX, entry.getValue().longValue());
            }
            alter.execute(unit, entry.getValue(), new AlterAfterValue(), 1);
        }

        unit.setOriginValues(unit.getValues());
    }

    /**
     * 召唤物默认从角色当前朝向、间隔相同角度、顺时针或逆时针依次生成
     * <p/>
     *
     * @param owner    召唤者，仅使用其坐标
     * @param curIndex 一次召唤多个召唤物时，召唤物的编号
     * @param dist
     * @param angle
     * @return 当前编号召唤物的出生位置
     */
    private Point defaultSummonPoint(Unit owner, int curIndex, int dist, int angle) {
        if (dist == 0 && angle == 0) {
            return owner.getPoint();
        }

        //  获取召唤者真实朝向
        final Unit target = owner.getTarget();
        final Point ownerPoint = owner.getPoint();
        Point targetPoint;
        if (target != null) {
            targetPoint = target.getPoint();
        } else {
            targetPoint = owner.getFriend().isAttacker() ? new Point(BattleConstant.MAX_X, ownerPoint.y) : new Point(0, ownerPoint.y);
        }

        //根据配置的角度和召唤物索引决定召唤物出生坐标需要旋转的角度
        final double degrees = Math.PI * angle / 180 * (curIndex + 1);

        if (targetPoint.getX() > ownerPoint.getX()) {
            //生成起始点
            final Point startPoint = new Point(ownerPoint.getX() + dist, ownerPoint.y);
            //面朝右逆时针旋转
            int x2 = (int) ((startPoint.getX() - ownerPoint.getX()) * Math.cos(degrees) - (startPoint.getY() - ownerPoint.getY()) * Math.sin(degrees) + ownerPoint.getX());
            int y2 = (int) ((startPoint.getY() - ownerPoint.getY()) * Math.cos(degrees) + (startPoint.getX() - ownerPoint.getX()) * Math.sin(degrees) + ownerPoint.getY());

            return correctPoint(x2, y2);
        } else {
            final Point startPoint = new Point(ownerPoint.getX() - dist, ownerPoint.y);
            //面朝左顺时针旋转
            int x2 = (int) ((startPoint.getX() - ownerPoint.getX()) * Math.cos(degrees) + (startPoint.getY() - ownerPoint.getY()) * Math.sin(degrees) + ownerPoint.getX());
            int y2 = (int) ((startPoint.getY() - ownerPoint.getY()) * Math.cos(degrees) - (startPoint.getX() - ownerPoint.getX()) * Math.sin(degrees) + ownerPoint.getY());
            return correctPoint(x2, y2);
        }
    }

    private Point correctPoint(int x, int y) {
        if (x < 0) {
            x = 0;
        } else if (x > BattleConstant.MAX_X) {
            x = BattleConstant.MAX_X;
        }
        if (y < 0) {
            y = 0;
        } else if (y > BattleConstant.MAX_Y) {
            y = BattleConstant.MAX_Y;
        }
        return new Point(x, y);
    }
}
