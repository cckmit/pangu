package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

import java.util.Map;
import java.util.TreeMap;

public class FengKuangBoShiZSParam {


    private Map<Integer, String> buffIds;

    @Getter
    private int selectTimes;

    @Getter
    private String selectId;

    public Map<Integer, String> getBuffIds() {
        if (!(buffIds instanceof TreeMap)) {
            buffIds = new TreeMap<>(buffIds);
        }
        return buffIds;
    }
}
