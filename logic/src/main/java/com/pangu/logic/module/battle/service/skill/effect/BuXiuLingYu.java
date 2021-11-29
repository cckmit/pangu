package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.AreaType;
import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.FilterType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.filter.utils.AreaFilter;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.AreaParam;
import com.pangu.logic.module.battle.service.skill.param.BuXiuLingYuParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 不朽领域:
 * 开场技能，入场时在己方后排张开结界，结界内友方目标攻击力提升12%；结界内敌方目标攻击力降低25%，移动速度降低30%。结界持续时长15秒
 * 2级:提升攻击力增加至16%
 * 3级:提升攻击力增加至20%
 * 4级:结界内的敌人降低能量恢复20%
 */
@Component
public class BuXiuLingYu implements SkillEffect {

    @Override
    public EffectType getType() {
        return EffectType.BU_XIU_LING_YU;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final BuXiuLingYuParam param = state.getParam(BuXiuLingYuParam.class);
        AreaParam areaParam = param.getArea();
        //为防守方时，修改坐标位置
        if (!owner.getFriend().isAttacker() && areaParam.getShape() == AreaType.RECTANGLE) {
            areaParam = AreaFilter.calEnemyLocations(areaParam);
        }


        //当前技能释放时，获取指定区域内的敌方和我方
        final List<Unit> enemies = FilterType.ENEMY.filter(owner, time);
        final List<Unit> friends = FilterType.FRIEND.filter(owner, time);
        List<Unit> enemiesInArea = AreaFilter.filterUnitInArea(enemies, areaParam);
        List<Unit> friendsInArea = AreaFilter.filterUnitInArea(friends, areaParam);


        //对友方释放buff
        for (Unit friend : friendsInArea) {
            BuffFactory.addBuff(param.getBuffId(), owner, friend, time, skillReport, null);
            //  让目标进入上下文，便于触发其他被动
            context.addValue(friend, AlterType.HP, 1);
        }
        //对敌方释放buff
        for (Unit enemy : enemiesInArea) {
            BuffFactory.addBuff(param.getDebuffId(), owner, enemy, time, skillReport, null);
        }

        //添加作用区域战报
        if (context.getLoopTimes() == 1) {
            skillReport.setAreaParam(areaParam, time);
        }
    }
}
