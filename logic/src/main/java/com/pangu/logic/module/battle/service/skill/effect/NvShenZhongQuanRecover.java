package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.Hp;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 女神忠犬
 * 战斗开始时,消耗自身60%当前生命值,召唤出3个小狗,小狗持续自动袭击敌人造成50%攻击力的伤害；小狗回到身边会回复造成伤害35%的生命值；当受到致死伤害时,会立即献祭一个小狗自身回复25%最大生命值
 * 2级:伤害提升至60%攻击力
 * 3级:伤害提升至70%攻击力
 * 4级:伤害提升至80%攻击力
 * <p>
 * 该技能是属于小狗的
 */
@Component
public class NvShenZhongQuanRecover implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.NV_SHEN_ZHONG_QUAN_RECOVER;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        //获取上次造成的伤害
        final List<PassiveState> passiveStates = owner.getPassiveStateByType(PassiveType.NV_SHEN_ZHONG_QUAN_MODE_SWITCH);
        if (CollectionUtils.isEmpty(passiveStates)) {
            return;
        }
        final PassiveState passiveState = passiveStates.get(0);
        final Long dmg = passiveState.getAddition(Long.class);
        if (dmg == null || dmg == 0) {
            return;
        }

        //计算回复比例
        final Double dmgRecoverRate = state.getParam(Double.class);
        final long recover = -(long) (dmgRecoverRate * dmg);

        //为主人回复生命
        final Unit summoner = owner.getSummonUnit();
        context.addValue(summoner, AlterType.HP, recover);
        skillReport.add(time, summoner.getId(), Hp.of(recover));
        passiveState.setAddition(0L);
    }
}
