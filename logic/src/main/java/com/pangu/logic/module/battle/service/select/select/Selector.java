package com.pangu.logic.module.battle.service.select.select;

import com.pangu.logic.module.battle.resource.SelectSetting;
import com.pangu.logic.module.battle.service.core.Unit;

import java.util.List;

/**
 * author weihongwei
 * date 2017/12/4
 */
public interface Selector {

    /**
     * 目标选择接口
     *
     * @param owner
     * @param time
     * @return
     */
    List<Unit> select(Unit owner, List<Unit> units, SelectSetting selectSetting, int time);
}
