package com.pangu.logic.module.battle.service.buff.effect;

import com.pangu.logic.module.battle.model.*;
import com.pangu.logic.module.battle.model.report.BuffReport;
import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.BattleConstant;
import com.pangu.logic.module.battle.service.buff.Buff;
import com.pangu.logic.module.battle.service.buff.BuffState;
import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.buff.param.ShiHuangZhiRenZSParam;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.TargetSelector;
import com.pangu.logic.module.battle.service.skill.effect.BuffUpdate;
import com.pangu.logic.module.battle.service.skill.param.AreaParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * 1：统帅友军，身后扇形区域攻速+20%
 * 10：身后友军攻速+35%
 * 20：统帅2名英雄时，自身每秒回复已损失血量的6%
 * 30：身后友军暴击伤害+25%
 */
@Component
public class ShiHuangZhiRenZS implements Buff {
    @Autowired
    private BuffUpdate buffUpdate;

    @Override
    public BuffType getType() {
        return BuffType.SHI_HUANG_ZHI_REN_ZS;
    }

    @Override
    public void update(BuffState state, Unit unit, int time, Object addition) {
        final ShiHuangZhiRenZSParam param = state.getParam(ShiHuangZhiRenZSParam.class);

        //选取目标点以固定扇形的方向，扇形中轴始终平行于x轴。
        Point targetPoint;
        if (unit.getTarget() != null) {
            //当角色存在faceTarget时，x坐标取目标x关于自身的对称点
            targetPoint = new Point(unit.getPoint().y * 2 - unit.getTarget().getPoint().x, unit.getPoint().y);
        } else {
            //根据敌我阵营作为依据
            targetPoint = new Point(unit.getFriend().isAttacker() ? 0 : BattleConstant.MAX_X, unit.getPoint().y);
        }

        //构造扇形选择器
        final SelectSetting fanShapedSelectSetting = SelectSetting.builder()
                .selectType(SelectType.COORDINATE_FAN_SHAPED)
                .distance(param.getRadiusForFanShaped())
                .width(param.getAngleForFanShaped())
                .realParam(
                        AreaParam.builder()
                                .shape(AreaType.POINT)
                                .points(new int[][]{{targetPoint.x, targetPoint.y}})
                                .build()
                )
                .filter(FilterType.FRIEND)
                .sortType(SortType.DISTANCE)
                .build();

        //筛选命中目标
        final List<Unit> targets = TargetSelector.select(unit, time, fanShapedSelectSetting);
        targets.remove(unit);

        final BuffReport buffReport = state.getBuffReport();
        //为目标添加增益
        for (Unit target : targets) {
            buffUpdate.doBuffUpdate(param.getBuffForFriend(), unit, target, buffReport, time);
        }
        //目标数量满足条件时为自身添加增益
        if (param.getHotBuff() == null) {
            return;
        }
        if (targets.size() < param.getTriggerCountOfHot()) {
            return;
        }
        buffUpdate.doBuffUpdate(param.getHotBuff(), unit, unit, buffReport, time);
    }
}
