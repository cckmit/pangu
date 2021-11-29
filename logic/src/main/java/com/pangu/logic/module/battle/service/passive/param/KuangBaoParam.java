package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.CalType;
import lombok.Getter;

import java.util.Map;

@Getter
public class KuangBaoParam {
    //每降低多少生命值，触发一次加成
    private long triggerHpCut;
    //每降低多少百分比生命值，触发一次加成
    private double triggerHpPctCut;

    //加成
    private double factor;
    private CalType calType;
    private Map<AlterType, String> alters;
}
