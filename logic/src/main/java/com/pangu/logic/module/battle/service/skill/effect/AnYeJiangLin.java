package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.ItemAdd;
import com.pangu.logic.module.battle.service.buff.BuffFactory;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.select.utils.SquareInCircle;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.AnYeJiangLinParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 暗夜降临
 * 向敌阵中投掷四根羽毛形成法阵，对范围内的敌入造成270%攻击力的伤害。法阵持续11秒，自身在法阵中攻击速度提升10%
 * 2级:持续时间提升至13秒
 * 2级：攻击速度提升至20%
 * 4级:持续时间提升至15秒
 */
@Component
public class AnYeJiangLin implements SkillEffect {
    @Override
    public EffectType getType() {
        return EffectType.AN_YE_JIANG_LIN;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        AnYeJiangLinParam param = state.getParam(AnYeJiangLinParam.class);
        // 给自己添加buff
        // 设置自己的位置为圆心
        String buffId = param.getBuff();
        BuffFactory.addBuff(buffId, owner, owner, time, skillReport, null);
        // 生成飞镖
        Point[] square = SquareInCircle.square(owner.getPoint(), param.getDistance());
        List<ItemAdd> items = owner.addItem(time, square);
        for (ItemAdd item : items) {
            skillReport.add(time, owner.getId(), item);
        }
    }
}
