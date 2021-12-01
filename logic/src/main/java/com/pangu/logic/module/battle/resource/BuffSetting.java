package com.pangu.logic.module.battle.resource;

import com.pangu.logic.module.battle.service.buff.BuffType;
import com.pangu.logic.module.battle.service.buff.DispelType;
import com.pangu.framework.resource.Validate;
import com.pangu.framework.resource.anno.Id;
import com.pangu.framework.resource.anno.Resource;
import com.pangu.framework.utils.json.JsonUtils;
import com.pangu.framework.utils.lang.NumberUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Set;

/**
 * BUFF效果配置对象
 */
@Resource("battle")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BuffSetting implements Validate {

    //  BUFF效果标识
    @Id
    private String id;
    //  BUFF效果类型
    private BuffType type;

    // BUFF类型
    private String tag;

    // BUFF分类
    private Set<String> classifys = Collections.emptySet();

    // 总生效时间
    private int time;

    //  循环生效间隔
    private int interval;

    //  驱散类型
    private DispelType dispelType;

    //  执行时所需的上下文内容
    private String ctx;

    // 真实参数
    private Object realParam;

    //是否可以重复叠加
    private boolean repeat;

    //优先级，高的覆盖低的
    private int level;

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
        if (paramType.isPrimitive()) {
            realParam = NumberUtils.valueOf(paramType, ctx);
            return true;
        }
        realParam = JsonUtils.string2Object(ctx, paramType);
        return true;
    }


    final private static String SPLIT = "_LV";
    public static String getPrefix(String buffId) {
        return buffId.split("_LV")[0];
    }
}
