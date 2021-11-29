package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

/**
 * 例：
 * newPrefix = "NEW:";
 * oldPrefix = "WU_WEI_XUAN_FENG";
 * replace("WU_WEI_XUAN_FENG_LV1") = "NEW:WU_WEI_XUAN_FENG_LV1";
 * replace("WU_WEI_LV1") = NULL;
 */
@Getter
public class SkillReplaceByIdPrefixParam {
    private String newPrefix = "";
    private String oldPrefix;

    /** 替换条件*/
    private String cond;

    /**
     * @param oldId
     * @return 新技能id。null：oldId不属于可替换技能
     */
    public String replace(String oldId) {
        return oldId.startsWith(oldPrefix) ? newPrefix + oldId : null;
    }
}
