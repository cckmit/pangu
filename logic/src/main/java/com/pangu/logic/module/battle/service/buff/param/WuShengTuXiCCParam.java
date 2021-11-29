package com.pangu.logic.module.battle.service.buff.param;

import com.pangu.logic.module.battle.service.passive.PassiveType;
import com.pangu.logic.module.battle.service.passive.param.WuShengTuXiParam;
import lombok.Getter;

@Getter
public class WuShengTuXiCCParam extends WuShengTuXiParam {
    /**
     * 内置cd
     */
    private int cd;

    /**
     * 替换的被动类型
     */
    private PassiveType passiveType;

    /**
     * 替换新技能的前缀
     */
    private String newPrefix;

    public String replace(String oldId) {
        return newPrefix + oldId;
    }
}
