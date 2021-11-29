package com.pangu.logic.module.battle.service.skill.param;

import lombok.Getter;

@Getter
public class DaDiPaoXiaoZSParam {

    /** 检测间隔时间（单位：毫秒） */
    private int interval = 1000;
    /** 范围目标 */
    private String selectId;
    /** 攻击力BUFF标识 */
    private String attBuffId;
    /** 治疗BUFF所需在场人数 */
    private int cureRequire;
    /** 治疗BUFF标识 */
    private String cureBuffId;

}
