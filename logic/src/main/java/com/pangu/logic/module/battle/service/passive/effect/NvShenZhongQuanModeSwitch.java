package com.pangu.logic.module.battle.service.passive.effect;

import com.pangu.logic.module.battle.model.FilterType;
import com.pangu.logic.module.battle.model.SelectType;
import com.pangu.logic.module.battle.model.UnitState;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.*;
import com.pangu.logic.module.battle.service.passive.param.NvShenZhongQuanModeSwitchParam;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.skill.SkillFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 女神忠犬(3级)
 * 战斗开始时，消耗自身60%当前生命
 * 值，召唤出3个小狗，小狗持续自动袭击敌人造成65%攻击力的伤害；
 * 小狗回到身边会回复造成伤害30%的生命值；
 * 当受到致死伤害时，会立即献祭一个小狗自身回复25%最大生命值。
 * 2级:伤害提升至70%攻击力
 * 3级:伤害提升至75%攻击力
 * <p>
 * 该被动挂载于小狗身上
 */
@Component
public class NvShenZhongQuanModeSwitch implements AttackPassive, SkillSelectPassive, SkillReleasePassive {

    @Override
    public void attack(PassiveState passiveState, Unit owner, Unit target, long damage, int time, Context context, SkillState skillState, SkillReport skillReport) {
        //攻击完敌人后，将造成的伤害缓存
        if (owner.getEnemy() == target.getFriend()) {
            passiveState.setAddition(damage);
        }
    }

    @Override
    public PassiveType getType() {
        return PassiveType.NV_SHEN_ZHONG_QUAN_MODE_SWITCH;
    }

    @Override
    public SkillState skillSelect(PassiveState passiveState, SkillState skillState, Unit owner, int time) {
        //  当追踪目标非召唤者时，默认执行攻击技能
        if (owner.getTraceUnit() != owner.getSummonUnit()) {
            return null;
        }
        //  当追踪目标为召唤者时，执行治疗技能
        return SkillFactory.initState(passiveState.getParam(NvShenZhongQuanModeSwitchParam.class).getDmgTriggerSkill());
    }

    @Override
    public void skillRelease(PassiveState passiveState, Unit owner, Unit attacker, SkillState skillState, int time, Context context, SkillReport skillReport) {
        if (owner != attacker) {
            return;
        }
        final NvShenZhongQuanModeSwitchParam param = passiveState.getParam(NvShenZhongQuanModeSwitchParam.class);
        final String skillStateId = skillState.getId();
        //  治疗完召唤者后，将追踪目标切换为随机敌方单元
        if (skillStateId.equals(param.getRecoverTriggerSkill())) {
            owner.addState(UnitState.ZHUI_JI, Integer.MAX_VALUE);
            owner.setTraceUnit(owner.getSummonUnit());
        }
        //  攻击完目标后，将追踪目标切换为召唤者
        if (skillStateId.equals(param.getDmgTriggerSkill())) {
            final SelectSetting selectSetting = SelectSetting.builder()
                    .filter(FilterType.ENEMY)
                    .selectType(SelectType.RANDOM)
                    .count(1)
                    .build();
            final List<Unit> randomEnemies = TargetSelector.select(owner, time, selectSetting);
            if (CollectionUtils.isEmpty(randomEnemies)) {
                owner.setTraceUnit(null);
            } else {
                owner.setTraceUnit(randomEnemies.get(0));
            }
        }
    }
}
