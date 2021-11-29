package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.*;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.BattleConstant;
import com.pangu.logic.module.battle.service.action.Action;
import com.pangu.logic.module.battle.service.action.SkillEffectAction;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.select.select.utils.BestCircle;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.AreaParam;
import com.pangu.logic.module.battle.service.skill.param.MoRiShenPanElectricParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 末日审判
 * 判断当前场上敌方职业,若物理攻击类职业更多时则召唤混沌陨石砸向目标,对目标范围内的所有敌人造成155%伤害,且眩晕1秒,流星着陆后会一直向前滚动,形成火焰路径,站在火焰路径上的玩家会造成每秒40%魔法伤害,持续3秒；若法系攻击类的职业更多时则会召唤电磁球,扔向指定位置,每秒降低周围敌人60能量,持续3秒,3秒后脉冲炸开,对范围内的敌人造成165%的伤害
 * 2级:眩晕时间提升至1.5秒
 * 3级:能量降低提升至每秒85
 * 4级:混沌陨石伤害提升至175%,电磁脉冲伤害提升至190%
 * <p>
 * 该类为电球实现
 */
@Component
public class MoRiShenPanElectric implements SkillEffect {
    @Autowired
    private MpChange mpChange;
    @Autowired
    private HpMagicDamage magicDamage;

    @Override
    public EffectType getType() {
        return EffectType.MO_RI_SHEN_PAN_ELECTRIC;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final MoRiShenPanElectricParam param = state.getParam(MoRiShenPanElectricParam.class);
        final SkillEffectAction rootSkillEffectAction = context.getRootSkillEffectAction();

        //首次循环时计算电球位置
        final List<Point> enemiesPoints = FilterType.ENEMY.filter(owner, time).stream().map(Unit::getPoint).collect(Collectors.toList());
        final Point bestCenter = BestCircle.calBestPoint(owner.getPoint(), enemiesPoints, param.getRadius(), BattleConstant.MAX_X, BattleConstant.MAX_Y, false);
        final AreaParam bestCircle = AreaParam.builder()
                .shape(AreaType.CIRCLE)
                .r(param.getRadius())
                .points(new int[][]{{bestCenter.x, bestCenter.y}})
                .build();
        final SelectSetting circleSelectSetting = SelectSetting.builder()
                .filter(FilterType.ENEMY)
                .selectType(SelectType.AREA)
                .realParam(bestCircle)
                .build();
        rootSkillEffectAction.setAddition(circleSelectSetting);

        //弹道飞行时间计算
        int flyTime = 0;
        final int trackSpeed = skillState.getTrackSpeed();
        if (trackSpeed > 0) {
            final int dist = bestCenter.distance(owner.getPoint());
            flyTime = SkillEffectAction.getFlyTime(dist, trackSpeed);
        }
        final int actTime = flyTime + time;

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
                final Context context = new Context(owner);
                final SelectSetting circleSelectSetting = rootSkillEffectAction.getAddition(SelectSetting.class);

                //加入区域战报
                if (curLoop == 1) {
                    skillReport.setAreaParam(circleSelectSetting.getRealParam(AreaParam.class), time);
                }

                final List<Unit> targetsInCircle = TargetSelector.select(owner, time, circleSelectSetting);
                final EffectState effectState = new EffectState(state.getSetting(), 0);
                effectState.setParamOverride(param.getMpChange());

                //循环时扣减目标能量
                for (Unit unit : targetsInCircle) {
                    context.execAttackBeforePassiveAndEffect(time, mpChange, unit, skillState, effectState, skillReport);
                }

                //最后一次循环时爆炸造成伤害
                if (curLoop == exeTimes) {
                    effectState.setParamOverride(param.getDmg());
                    for (Unit unit : targetsInCircle) {
                        context.execAttackBeforePassiveAndEffect(time, magicDamage, unit, skillState, effectState, skillReport);
                    }
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
