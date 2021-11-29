package com.pangu.logic.module.battle.service.buff.param;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.CalType;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class OnceParam {

    // 参数比率
    private double factor;

    // 计算方式
    private CalType calType;

    // 属性值
    private Map<AlterType, String> alters;

    // 添加被动
    private String passive;

}
