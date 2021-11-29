package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.FilterType;
import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.model.SortType;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.model.report.values.ItemAdd;
import com.pangu.logic.module.battle.model.report.values.ItemMove;
import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.select.select.utils.Circle;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.XingBaoNewParam;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * 星爆:
 * 开战后,每过7秒角色会指挥占星球,朝敌人最虚弱的目标发射,指令结束后占星球会停在目标区域，且对该区域的敌人造成190%的伤害 <br>
 * 2级:伤害提升至220% <br>
 * 3级:触发时间降低为5秒 <br>
 * 4级:伤害提升至245% <br>
 */
@Component
public class XingBaoNew implements SkillEffect {
    @Autowired
    HpMagicDamage hpMagicDamage;

    @Override
    public EffectType getType() {
        return EffectType.XING_BAO_NEW;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final XingBaoNewParam param = state.getParam(XingBaoNewParam.class);

        //定位最虚弱目标
        final SelectSetting selectHpLowest = SelectSetting.builder()
                .filter(FilterType.ENEMY)
                .sortType(SortType.HP_LOW)
                .count(1)
                .build();
        final List<Unit> enemies = TargetSelector.select(owner, time, selectHpLowest);
        if (CollectionUtils.isEmpty(enemies)) {
            return;
        }
        final Point itemDestination = enemies.get(0).getPoint();

        //若场上存在道具，则添加，否则移动
        final LinkedList<ItemAdd> items = owner.getItems();
        if (CollectionUtils.isEmpty(items)) {
            final int validTime = owner.getBattle().getConfig().getTime();
            final ItemAdd itemAdd = owner.addItem(time, ArrayUtils.toArray(itemDestination), validTime).get(0);
            final Point pointForReport = new Point(itemAdd.getPoint());
            final ItemAdd itemAddForReport = new ItemAdd(itemAdd.getId(), pointForReport, validTime);
            skillReport.add(time, owner.getId(), itemAddForReport);
        } else {
            final HashMap<ItemAdd, Point> itemAddPointHashMap = new HashMap<>();
            itemAddPointHashMap.put(items.get(0), itemDestination);
            final List<ItemMove> itemMoves = owner.moveItem(itemAddPointHashMap);
            for (ItemMove itemMove : itemMoves) {
                skillReport.add(time, owner.getId(), itemMove);
            }
        }
        

        //对最虚弱目标及其周围目标造成伤害
        final Circle dmgCircle = new Circle(itemDestination.x, itemDestination.y, param.getRadius());
        state.setParamOverride(param.getDamageParam());
        for (Unit enemy : FilterType.ENEMY.filter(owner, time)) {
            final Point point = enemy.getPoint();
            if (dmgCircle.inShape(point.x, point.y)) {
                hpMagicDamage.execute(state, owner, enemy, skillReport, time, skillState, context);
            }
        }
        state.setParamOverride(null);
    }
}
