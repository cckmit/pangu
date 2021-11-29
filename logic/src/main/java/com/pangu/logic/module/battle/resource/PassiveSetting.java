package com.pangu.logic.module.battle.resource;

import com.pangu.logic.module.battle.service.core.Unit;
import com.pangu.logic.module.battle.service.passive.PassiveState;
import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.framework.resource.Validate;
import com.pangu.framework.resource.anno.Id;
import com.pangu.framework.resource.anno.Resource;
import com.pangu.framework.utils.json.JsonUtils;
import com.pangu.framework.utils.lang.NumberUtils;
import lombok.Getter;

/**
 * 被动效果配置对象
 */
@Resource("battle")
@Getter
public class PassiveSetting implements Validate {

    //  被动效果标识
    @Id
    private String id;

    //  被动效果类型
    private PassiveType type;

    //  生效次数(null表示永远生效)
    private int times;

    // 冷却时间
    private int coolTime;

    // 存在时间
    private int time;

    //  效果执行时所需的公式上下文内容
    private String ctx;

    //  是否可被封印
    private boolean sealable = true;

    // 真实参数
    private Object realParam;

    @Override
    public boolean isValid() {
        if (realParam != null) {
            return true;
        }
        Class<?> paramType = type.getClz();
        if (paramType == String.class) {
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

    /**
     * 与客户端相同的分隔符
     */
    final private static String SPLIT = "_LV";

    public static String getPrefix(String passiveId) {
        return passiveId.split("_LV")[0];
    }

    public static int getLv(String passiveId) {
        final String[] prefixAndLv = passiveId.split("_LV");
        if (prefixAndLv.length != 2) {
            return 0;
        }
        final String lv = prefixAndLv[1];
        try {
            return Integer.valueOf(lv);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static String toPassiveId(Unit unit, String prefix) {
        for (PassiveState passiveState : unit.getPassiveStatesById().values()) {
            final String id = passiveState.getId();
            if (getPrefix(id).equals(prefix)) {
                return id;
            }
        }
        return null;
    }

    public static String toPassiveId(String prefix, int lv) {
        if (lv == 0) {
            return prefix;
        }
        return prefix + SPLIT + lv;
    }
}
