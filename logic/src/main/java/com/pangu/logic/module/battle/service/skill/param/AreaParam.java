package com.pangu.logic.module.battle.service.skill.param;

import com.pangu.logic.module.battle.model.AreaType;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.select.filter.utils.AreaFilter;
import com.pangu.logic.module.battle.service.select.select.utils.Circle;
import com.pangu.logic.module.battle.service.select.select.utils.Rectangle;
import com.pangu.framework.protocol.annotation.Transable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Transable
public class AreaParam {
    //区域类型
    AreaType shape;
    //固定区域所需的坐标列表
    int[][] points;
    //半径
    int r;

    public static AreaParam from(Circle circle) {
        return AreaParam.builder()
                .shape(AreaType.CIRCLE)
                .r(circle.getRadius())
                .points(new int[][]{{circle.getCenterX(), circle.getCenterY()}})
                .build();
    }

    public static AreaParam from(Rectangle rectangle) {
        return AreaParam.builder()
                .shape(AreaType.RECTANGLE)
                .points(Arrays.stream(rectangle.getPs()).map(p -> new int[]{p.x, p.y}).toArray(int[][]::new))
                .build();
    }

    public List<Unit> inArea(List<Unit> units) {
        return AreaFilter.filterUnitInArea(units, this);
    }
}
