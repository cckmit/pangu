package com.pangu.logic.module.battle.model;

import com.pangu.framework.protocol.annotation.Transable;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 多队队伍构建信息
 */
@Transable
@Data
public class FormationBuildInfos {
    private Map<Integer, FormationBuildInfo> formations = new HashMap<>(4, 1);

    public void put(Integer index, FormationBuildInfo buildInfo) {
        formations.put(index, buildInfo);
    }
}
