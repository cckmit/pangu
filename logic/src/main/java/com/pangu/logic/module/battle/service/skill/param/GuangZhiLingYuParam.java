package com.pangu.logic.module.battle.service.skill.param;

import com.pangu.logic.module.battle.model.FilterType;
import com.pangu.logic.module.battle.service.passive.param.BuffUpdateParam;
import lombok.Getter;

@Getter
public class GuangZhiLingYuParam {
    //结界半径
    private int r;

    //结界增益
    private BuffUpdateParam buff;

    //影响目标过滤器
    private FilterType filterType = FilterType.FRIEND;
}
