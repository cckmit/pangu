package com.pangu.logic.module.battle.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.pangu.logic.module.battle.model.FilterType;
import com.pangu.logic.module.battle.model.SelectType;
import com.pangu.logic.module.battle.model.SortType;
import com.pangu.framework.resource.anno.Id;
import com.pangu.framework.resource.anno.Resource;
import com.pangu.framework.utils.json.JsonUtils;
import lombok.*;

@Resource("battle")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class SelectSetting {

    @Id
    private String id;

    // 过滤器 范围目标在过滤器的结果中选择
    private FilterType filter;

    // 范围目标类型
    private SelectType selectType;

    // 目标距离(圆形目标为半径，矩形目标为长)
    private int distance;

    // 矩形目标宽度(或者扇形角度)
    private int width;

    // 排序方式(默认为距离)
    private SortType sortType;

    // 技能目标数量
    private int count;

    //附加信息
    private String addition;

    //对应的真实附加信息
    public Object realParam;

    public <T> T getRealParam(Class<T> clazz) {
        if (realParam != null) {
            return (T) realParam;
        }
        realParam = JsonUtils.string2Object(addition, clazz);
        return (T) realParam;
    }

    public <T> T getRealParam(TypeReference<T> reference) {
        if (realParam != null) {
            return (T) realParam;
        }
        realParam = JsonUtils.string2GenericObject(addition, reference);
        return (T) realParam;
    }
}
