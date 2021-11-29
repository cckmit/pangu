package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.AttackPassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.BingShuangAiQuParam;
import com.pangu.framework.utils.math.RandomUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 冰霜哀曲
 * 冰封世界技能造成伤害时,有50%概率会使目标(最多2人)进入压制状态2秒(回能速度降低30%)
 * 2级:压制状态提升至3.5秒
 * 3级:目标数量提升至5人
 * 4级:压制时回能速度降低至50%
 */
@Component
public class BingShuangAiQu implements AttackPassive {
    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        if (!skillState.getTag().equals("bing_feng_shi_jie")) {
            return;
        }
        if (damage >= 0) {
            return;
        }
        final BingShuangAiQuParam param = passiveState.getParam(BingShuangAiQuParam.class);

        //当场上被压制目标数量到达上限时，不再添加压制
        final String buffId = param.getBuff().getBuffId();
        final List<Unit> enemies = target.getFriend().getCurrent();
        final List<Unit> deBuffedEnemies = enemies.stream()
                .filter(enemy -> !CollectionUtils.isEmpty(enemy.getBuffBySettingId(buffId)))
                .collect(Collectors.toList());
        if (deBuffedEnemies.size() >= param.getMaxTriggerCount()) {
            return;
        }

        //当前目标身上已存在压制时，不再执行逻辑
        if (deBuffedEnemies.contains(target)) {
            return;
        }

        //是否触发
        if (!RandomUtils.isHit(param.getTriggerRate())) {
            return;
        }

        //成功执行
        BuffFactory.addBuff(buffId, owner, target, time, skillReport, null);
    }

    @Override
    public PassiveType getType() {
        return PassiveType.BING_SHUANG_AI_QU;
    }
}
