package com.pangu.logic.module.battle.service.select;

import com.pangu.logic.module.battle.model.FilterType;
import com.pangu.logic.module.battle.model.SelectType;
import com.pangu.logic.module.battle.model.SortType;
import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.framework.resource.Storage;
import com.pangu.framework.resource.anno.Static;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;

/**
 * author weihongwei
 * date 2017/11/14
 */
@Component
public class TargetSelector {

    @Static
    private Storage<String, SelectSetting> settingStorage;

    private static Storage<String, SelectSetting> storage;

    @PostConstruct
    void init() {
        storage = settingStorage;
    }

    public static List<Unit> select(Unit owner, String selectId, int time) {
        SelectSetting selectSetting = storage.get(selectId, true);
        return select(owner, time, selectSetting);
    }

    public static List<Unit> select(Unit owner, int time, SelectSetting selectSetting) {
        FilterType filter = selectSetting.getFilter();
        List<Unit> select = filter.filter(owner, time);
        if (select.isEmpty()) {
            return select;
        }
        SelectType selectType = selectSetting.getSelectType();

        if (selectType != null) {
            // 范围目标选择，仅仅按照圆形，矩形，单点目标选择
            select = selectType.select(owner, select, selectSetting, time);
            if (CollectionUtils.isEmpty(select)) {
                return Collections.emptyList();
            }
        }
        int count = selectSetting.getCount();
        if (count <= 0) {
            return select;
        }
        if (count >= select.size()) {
            return select;
        }

        // 按照排序规则
        SortType sortType = selectSetting.getSortType();
        if (sortType != null && select.size() > 1) {
            select = sortType.sort(owner, select, selectSetting);
        }

        return select.subList(0, count);
    }
}
