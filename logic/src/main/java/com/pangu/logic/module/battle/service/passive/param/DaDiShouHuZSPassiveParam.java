package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public class DaDiShouHuZSPassiveParam {

    /** 图腾守护的技能标签 */
    private String skillTag;
    /** 存在护盾时，添加的BUFF标识 */
    private List<String> buffIds = Collections.emptyList();
    /** 嘲讽时，恢复的能量值 */
    private int sneerMpRecover;
    /** 嘲讽时，给对手加的BUFF标识 */
    private String sneerBuffId;

}
