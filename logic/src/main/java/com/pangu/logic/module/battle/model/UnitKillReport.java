package com.pangu.logic.module.battle.model;

import com.pangu.framework.protocol.annotation.Transable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Transable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnitKillReport {

    /**
     * 击杀单位ID
     */
    private String id;

    /**
     * 击杀时间
     */
    private int time;
}
