package com.pangu.logic.module.battle.resource;

import com.pangu.logic.module.battle.model.EffectType;
import com.pangu.framework.resource.Validate;
import com.pangu.framework.resource.anno.Id;
import com.pangu.framework.resource.anno.Resource;
import com.pangu.framework.utils.json.JsonUtils;
import com.pangu.framework.utils.lang.NumberUtils;
import lombok.Getter;

/**
 * 技能效果配置
 */
@Resource("battle")
@Getter
public class EffectSetting implements Validate {

    @Id
    private String id;

    //  技能效果类型
    private EffectType type;

    // 延时执行（锁定延时前的目标）
    private int playTime;
    // 延时执行（在执行时间到达后重新选择目标）
    private boolean reselectTargets;

    // 是否跳过命中率校验
    private boolean skipHitCheck;

    //  选择目标配置ID
    private String target;

    //  技能效果执行时所需的公式上下文内容
    private String ctx;

    //  是否为动态计算弹道，应对一个技能多个子弹弹道的情况
    private boolean dynamicPlayTime ;

    // 真实
    private Object realParam;

    @Override
    public boolean isValid() {
        if (realParam != null) {
            return true;
        }
        Class<?> paramType = type.getParamType();
        if (paramType == null || paramType == String.class) {
            realParam = ctx;
            return true;
        }
        if (paramType.isEnum()) {
            realParam = JsonUtils.convertObject(ctx, paramType);
            return true;
        }
        if (paramType.isPrimitive()) {
            realParam = NumberUtils.valueOf(paramType, ctx);
            return true;
        }
        realParam = JsonUtils.string2Object(ctx, paramType);
        return true;
    }
}
