package com.pangu.logic.module.battle.service.skill.effect;

import com.pangu.logic.module.battle.model.AreaType;
import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.logic.module.battle.model.Point;
import com.pangu.logic.module.battle.model.report.SkillReport;
import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.BattleConstant;
import com.pangu.logic.module.battle.service.core.Context;
import com.pangu.logic.module.battle.service.core.EffectState;
import com.pangu.logic.module.battle.service.core.SkillState;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.select.utils.BestCircle;
import com.pangu.logic.module.battle.service.skill.SkillEffect;
import com.pangu.logic.module.battle.service.skill.param.AreaParam;
import com.pangu.framework.resource.Storage;
import com.pangu.framework.resource.anno.Static;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 打印bestCircle区域战报
 */
@Component
public class BestCircleAreaReportGenerator implements SkillEffect {
    @Static
    private Storage<String, SelectSetting> settingStorage;

    @Override
    public EffectType getType() {
        return EffectType.BEST_CIRCLE_AREA_REPORT_GENERATOR;
    }

    @Override
    public void execute(EffectState state, Unit owner, Unit target, SkillReport skillReport, int time, SkillState skillState, Context context) {
        final AreaParam preEffectArea = skillReport.getAreaParam(time);
        if (preEffectArea != null) {
            return;
        }

        final String targetId = state.getParam(String.class);
        final SelectSetting selectSetting = settingStorage.get(targetId, true);
        final List<Unit> targets = selectSetting.getFilter().filter(owner, time);
        final List<Point> points = targets.stream().map(Unit::getPoint).collect(Collectors.toList());
        final int radius = selectSetting.getWidth();
        final Point bestCenter = BestCircle.calBestPoint(owner.getPoint(), points, radius, BattleConstant.MAX_X, BattleConstant.MAX_Y, false);
        final AreaParam areaParam = AreaParam.builder()
                .shape(AreaType.CIRCLE)
                .points(new int[][]{{bestCenter.x, bestCenter.y}})
                .r(radius).build();
        skillReport.setAreaParam(areaParam, time);
    }
}
