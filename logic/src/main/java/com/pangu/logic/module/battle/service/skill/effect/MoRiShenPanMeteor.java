package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.*;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.action.Action;
import com.pangu.logic.module.battle.service.action.SkillEffectAction;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.select.select.utils.Rectangle;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.AreaParam;
import com.pangu.logic.module.battle.service.skill.param.MoRiShenPanMeteorParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 末日审判
 * 判断当前场上敌方职业,若物理攻击类职业更多时则召唤混沌陨石砸向目标,对目标范围内的所有敌人造成155%伤害,且眩晕1秒,流星着陆后会一直向前滚动,形成火焰路径,站在火焰路径上的玩家会造成每秒40%魔法伤害,持续3秒；若法系攻击类的职业更多时则会召唤电磁球,扔向指定位置,每秒降低周围敌人60能量,持续3秒,3秒后脉冲炸开,对范围内的敌人造成165%的伤害
 * 2级:眩晕时间提升至1.5秒
 * 3级:能量降低提升至每秒85
 * 4级:混沌陨石伤害提升至175%,电磁脉冲伤害提升至190%
 * <p>
 * 该类为陨石实现
 */
@Component
public class MoRiShenPanMeteor implements SkillEffect {
    @Autowired
    private HpMagicDamage hpMagicDamage;
    @Autowired
    private StateAddEffect stateAddEffect;
    @Autowired
    private Repel repel;

    @Override
    public EffectType getType() {
        return EffectType.MO_RI_SHEN_PAN_METEOR;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final Unit faceTarget = owner.getTarget();
        if (faceTarget == null) {
            return;
        }

        final MoRiShenPanMeteorParam param = state.getParam(MoRiShenPanMeteorParam.class);
        final SkillEffectAction rootSkillEffectAction = context.getRootSkillEffectAction();

        //计算弹道飞行时间
        int flyTime = 0;
        final int trackSpeed = skillState.getTrackSpeed();
        if (trackSpeed > 0) {
            final int dist = faceTarget.getPoint().distance(owner.getPoint());
            flyTime = SkillEffectAction.getFlyTime(dist, trackSpeed);
        }
        final int actTime = flyTime + time;

        final SelectSetting crashDmgSelectSetting = SelectSetting.builder()
                .filter(FilterType.ENEMY)
                .selectType(SelectType.TARGET_CIRCLE)
                .width(param.getRadius())
                .sortType(SortType.DISTANCE).build();

        final Action action = new Action() {
            final private int exeTimes = param.getExeTimes();
            final private int exeInterval = param.getExeInterval();
            private int curLoop = 0;
            private int time = actTime;

            @Override
            public int getTime() {
                return time;
            }

            @Override
            public void execute() {
                curLoop++;
                if (curLoop > exeTimes) {
                    return;
                }
                Context context = new Context(owner);
                if (curLoop == 1) {
                    final List<Unit> crashDmgTargets = TargetSelector.select(owner, time, crashDmgSelectSetting);
                    //首次执行时陨石撞击目标周围造成伤害和眩晕，并形成灼烧地形
                    final EffectState effectState = new EffectState(state.getSetting(), 0);
                    effectState.setParamOverride(param.getCrashDmg());
                    for (Unit crashDmgTarget : crashDmgTargets) {
                        context.execAttackBeforePassiveAndEffect(time, hpMagicDamage, crashDmgTarget, skillState, effectState, skillReport);
                    }
                    effectState.setParamOverride(param.getState());
                    for (Unit crashDmgTarget : crashDmgTargets) {
                        context.execAttackBeforePassiveAndEffect(time, stateAddEffect, crashDmgTarget, skillState, effectState, skillReport);
                    }
                    effectState.setParamOverride(null);

                    //沿用击退效果计算陨石路径，平行于X轴
                    int distance = param.getLength();
                    Point point = faceTarget.getPoint();
                    Point ownerPoint = owner.getPoint();
                    Point targetPoint = repel.calDestination(distance, point, ownerPoint);
                    Rectangle burningArea = new Rectangle(point, targetPoint, param.getWidth(), param.getLength());
                    final int[][] points = Arrays.stream(burningArea.getPs()).map(p -> new int[]{p.x, p.y}).toArray(int[][]::new);
                    final AreaParam areaParam = AreaParam.builder()
                            .shape(AreaType.RECTANGLE)
                            .points(points)
                            .build();
                    final SelectSetting burningAreaSelectSetting = SelectSetting.builder()
                            .filter(FilterType.ENEMY)
                            .selectType(SelectType.AREA)
                            .realParam(areaParam)
                            .build();
                    rootSkillEffectAction.setAddition(burningAreaSelectSetting);
                }

                //对灼烧地形中的目标造成伤害
                SelectSetting burningAreaSelectSetting = rootSkillEffectAction.getAddition(SelectSetting.class);
                final List<Unit> targets = TargetSelector.select(owner, time, burningAreaSelectSetting);
                final EffectState effectState = new EffectState(state.getSetting(), 0);
                effectState.setParamOverride(param.getDotDmg());
                for (Unit unit : targets) {
                    context.execAttackBeforePassiveAndEffect(time, hpMagicDamage, unit, skillState, effectState, skillReport);
                }
                effectState.setParamOverride(null);

                context.execute(time, skillState, skillReport);

                time += exeInterval;
                owner.addTimedAction(this);
            }
        };
        owner.addTimedAction(action);
    }
}
