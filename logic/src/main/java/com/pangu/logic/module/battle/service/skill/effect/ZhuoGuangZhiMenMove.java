package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.*;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.PositionChange;
import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.ZhuoGuangZhiMenParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 灼光之门
 * 通过灼光空间之门将当前生命值比例最高的敌人吸入灼光空间,如果成功吸入,则在3秒后后将其从最虚弱的敌入上方落下,造成120%攻击力的范围伤害,每12秒触发1次
 * 2级:伤害提升至140%攻击力
 * 3级:伤害提升至160%攻击力
 * 4级:触发时间降低为10秒
 */
@Component
public class ZhuoGuangZhiMenMove implements SkillEffect {
    @Autowired
    private HpMagicDamage hpMagicDamage;

    @Override
    public EffectType getType() {
        return EffectType.ZHUO_GUANG_ZHI_MEN_MOVE;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        //目标若未被驱逐则不执行任何效果
        if (!target.hasState(UnitState.EXILE, time)) {
            return;
        }
        target.removeState(UnitState.EXILE);
        final ZhuoGuangZhiMenParam param = state.getParam(ZhuoGuangZhiMenParam.class);
        final List<Unit> enemies = FilterType.ENEMY.filter(owner, time);
        Point destination;
        final Point targetPoint = target.getPoint();
        if (!CollectionUtils.isEmpty(enemies)) {
            //将目标移动至最虚弱目标身上
            destination = SortType.HP_LOW.sort(owner, enemies, SelectSetting.builder().build()).get(0).getPoint();
        } else {
            destination = targetPoint;
        }
        target.move(destination);
        skillReport.add(time, target.getId(), PositionChange.of(targetPoint.x, targetPoint.y));
        //对周围造成伤害
        final SelectSetting selectSetting = SelectSetting.builder()
                .filter(FilterType.FRIEND)
                .selectType(SelectType.SELF_CIRCLE)
                .sortType(SortType.DISTANCE)
                .distance(param.getDmgRadius()).build();
        final List<Unit> dmgTargets = TargetSelector.select(target, time, selectSetting);
        state.setParamOverride(param.getDmg());
        for (Unit dmgTarget : dmgTargets) {
            hpMagicDamage.execute(state, owner, dmgTarget, skillReport, time, skillState, context);
        }
        state.setParamOverride(null);
    }
}
