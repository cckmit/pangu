package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

import java.util.Map;
import java.util.TreeMap;

public class FengKuangBoShiZSParam {

    /** 对应炸弹所加的BUFF标识，<炸弹数量, BUFF标识> */
    private Map<Integer, String> buffIds;
    /** 变范围所需炸弹数量 */
    @Getter
    private int selectTimes;
    /** 变范围的目标选择 */
    @Getter
    private String selectId;

    public Map<Integer, String> getBuffIds() {
        if (!(buffIds instanceof TreeMap)) {
            buffIds = new TreeMap<>(buffIds);
        }
        return buffIds;
    }
}
