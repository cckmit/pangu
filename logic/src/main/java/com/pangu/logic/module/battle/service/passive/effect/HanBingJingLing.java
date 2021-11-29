package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.SkillType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.PassiveValue;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.DamagePassive;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.HanBingJingLingParam;
import com.pangu.logic.module.battle.service.skill.effect.SummonSkill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 寒冰精灵
 * 战斗开场,被人普攻5次后则会召唤出一个寒冰精灵(继承角色的属性),释放冰凌造成50%的伤害,且受到的伤害加深500%,精灵死亡后重置触发机制
 * 2级:精灵死亡时会对击杀者造成熔甲的效果,护甲降低30%,持续8秒
 * 3级:精灵受到的伤害加深降低为350%
 * 4级:精灵死亡时会造成爆炸效果,对周围的人造成200%的魔法伤害
 */
@Component
public class HanBingJingLing implements DamagePassive {
    @Autowired
    private SummonSkill summonSkill;

    @Override
    public void damage(PassiveState passiveState, Unit owner, long damage, Unit attacker, int time, Context context, SkillState skillState, SkillReport skillReport) {
        //非普攻不计数
        if (skillState.getType() != SkillType.NORMAL) {
            return;
        }
        //场上存在存活的的召唤单位时不执行被动
        final Addition addition = getAddition(passiveState);
        final List<Unit> summonUnits = addition.effectState.getAddition(List.class);
        if (summonUnits != null && summonUnits.stream().anyMatch(unit -> !unit.isDead())) {
            return;
        }

        final HanBingJingLingParam param = passiveState.getParam(HanBingJingLingParam.class);
        //计数
        addition.hitCount++;
        if (addition.hitCount < param.getTriggerCount()) {
            return;
        }
        //计数到达阈值，召唤单位，清空计数
        addition.hitCount = 0;
        summonSkill.doSummon(addition.effectState, owner, time, context, skillReport, PassiveValue.of(passiveState.getId(), owner.getId()));
        addition.trigger = attacker;
    }

    public Addition getAddition(PassiveState passiveState) {
        Addition addition = passiveState.getAddition(Addition.class);
        final HanBingJingLingParam param = passiveState.getParam(HanBingJingLingParam.class);
        if (addition == null) {
            addition = new Addition();
            addition.effectState.setParamOverride(param.getSummon());
            passiveState.setAddition(addition);
        }
        return addition;
    }

    private static class Addition {
        private int hitCount;
        private EffectState effectState = new EffectState(null, 0);
        private Unit trigger;
    }

    @Override
    public PassiveType getType() {
        return PassiveType.HAN_BING_JING_LING;
    }
}
