package com.pangu.logic.module.battle.service.skill.param;

import com.pangu.logic.module.battle.model.FilterType;
import com.pangu.logic.module.battle.service.passive.param.BuffUpdateParam;
import lombok.Getter;

@Getter
public class CangBaiJieJieParam {
    //封装伤害、目标选择等参数
    private AttackParam attackParam;

    //结界半径
    private int r;

    //对结界中的目标添加的debuff
    private BuffUpdateParam buff;

    //异常状态添加器
    private StateAddParam stateParam;

    //目标过滤器
    private FilterType filterType = FilterType.ENEMY;
}
