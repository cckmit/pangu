package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.FilterType;
import com.pangu.logic.module.battle.model.SortType;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.action.EffectAction;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.ZhuoGuangZhiMenParam;
import com.pangu.logic.module.battle.service.skill.utils.SkillUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * 灼光之门
 * 通过灼光空间之门将当前生命值比例最高的敌人吸入灼光空间,如果成功吸入,则在3秒后后将其从最虚弱的敌入上方落下,造成120%攻击力的范围伤害,每12秒触发1次
 * 2级:伤害提升至140%攻击力
 * 3级:伤害提升至160%攻击力
 * 4级:触发时间降低为10秒
 */
@Component
public class ZhuoGuangZhiMenExile implements SkillEffect {
    @Autowired
    private ZhuoGuangZhiMenMove zhuoGuangZhiMenMove;

    @Override
    public EffectType getType() {
        return EffectType.ZHUO_GUANG_ZHI_MEN_EXILE;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        //筛选出百分比生命值最高的目标
        final ZhuoGuangZhiMenParam param = state.getParam(ZhuoGuangZhiMenParam.class);
        final List<Unit> enemies = FilterType.ENEMY.filter(owner, time);
        if (CollectionUtils.isEmpty(enemies)) {
            return;
        }
        SortType.HP_PCT_LOW.sort(owner, enemies, SelectSetting.builder().build());
        final Unit targetToMove = enemies.get(enemies.size() - 1);

        //驱逐该目标
        final boolean success = SkillUtils.addState(owner, targetToMove, UnitState.EXILE, time, time + param.getMoveDelay(), skillReport, context);
        //驱逐失败则不执行后续伤害
        if (!success) {
            return;
        }

        //提交延迟移动并造成伤害的行为
        final EffectAction effectAction = new EffectAction(time + param.getMoveDelay(), owner, skillState, skillReport, state, Collections.singletonList(targetToMove));
        effectAction.setEffect(zhuoGuangZhiMenMove);
        if (skillState.getSetting().isIgnoreDie()) {
            owner.getBattle().addWorldAction(effectAction);
        } else {
            owner.addTimedAction(effectAction);
        }
    }
}
