package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.ItemAdd;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 1：开场自身周围生成3个乐章音符，可以对40%生命值以下的队友治疗25%最大生命，同一目标8秒间隔，消耗完毕后10秒再次生成
 * 10：可以对50%生命值以下的队友治疗35%最大生命，
 * 20：可以对60%生命值以下的队友治疗50%最大生命，
 * 30：受到治疗的队友+150能量
 */
@Component
public class XiaoSeZhiQinZS implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.XIAO_SE_ZHI_QIN_ZS;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        //需要生成的音符道具数量
        final int itemGenerateCount = state.getParam(Integer.class);
        final Point ownerPoint = owner.getPoint();
        final Point[] points = new Point[itemGenerateCount];
        for (int i = 0; i < itemGenerateCount; i++) {
            points[i] = ownerPoint;
        }

        //生成音符道具
        final List<ItemAdd> itemAdds = owner.addItem(time, points, owner.getBattle().getConfig().getTime());
//        final String ownerId = owner.getId();
//        for (ItemAdd itemAdd : itemAdds) {
//            skillReport.add(time, ownerId, itemAdd);
//        }
    }
}