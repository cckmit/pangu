package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.AreaType;
import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.SelectType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.AreaParam;
import com.pangu.logic.module.battle.service.skill.param.GuangZhiLingYuParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 光之领域
 * 制造一个圆形区域,区域内友方部队攻速提升10%,受到伤害降低10%屏障持续6秒
 * 2级:持续时间增加至8秒
 * 3级:受到的伤害降低20%
 * 4级:攻速提升至15%
 */
@Component
public class GuangZhiLingYu implements SkillEffect {
    @Autowired
    private BuffUpdate buffUpdate;

    @Override
    public EffectType getType() {
        return EffectType.GUANG_ZHI_LING_YU;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final GuangZhiLingYuParam param = state.getParam(GuangZhiLingYuParam.class);

        //结界类效果通过循环执行来实现，需要将目标选取策略缓存到skillEffectAction
        Map<String, SelectSetting> addition = context.getRootSkillEffectAction().getAddition(Map.class, new HashMap<String, SelectSetting>());
        SelectSetting dynamicSelectSetting = addition.get(state.getId());
        if (dynamicSelectSetting == null) {
            final AreaParam areaParam = AreaParam.builder()
                    .shape(AreaType.CIRCLE)
                    .points(new int[][]{{owner.getPoint().x, owner.getPoint().y}})
                    .r(param.getR())
                    .build();
            dynamicSelectSetting = SelectSetting.builder()
                    .realParam(areaParam)
                    .selectType(SelectType.AREA)
                    .filter(param.getFilterType())
                    .build();
            addition.put(state.getId(), dynamicSelectSetting);
        }

        //为选中目标添加BUFF
        final List<Unit> targets = TargetSelector.select(owner, time, dynamicSelectSetting);
        state.setParamOverride(param.getBuff());
        for (Unit unit : targets) {
            buffUpdate.execute(state, owner, unit, skillReport, time, skillState, context);
        }
        state.setParamOverride(null);
    }
}
