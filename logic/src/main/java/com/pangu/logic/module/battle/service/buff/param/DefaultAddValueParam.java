package com.pangu.logic.module.battle.service.buff.param;

import com.pangu.logic.module.battle.model.AlterType;
import com.pangu.logic.module.battle.model.CalType;
import lombok.Getter;

import java.util.Map;

/**
 * 添加属性默认参数（公用 有用到这个3个参数外 请自己创建新的Param）
 */
@Getter
public class DefaultAddValueParam {
    // 参数比率
    private double factor;

    // 计算方式
    private CalType calType;

    // 属性值
    private Map<AlterType, String> alters;

    // 生效目标
    private String targetId;

    // 生效条件
    private String updateCond;
}
