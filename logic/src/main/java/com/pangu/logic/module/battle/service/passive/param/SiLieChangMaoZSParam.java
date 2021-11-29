package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;

@Getter
public class SiLieChangMaoZSParam {

    /** 替换的普攻技能标识 */
    private String replaceSkillId;
    /** 暴击时，窃取的能量值 */
    private int critMp;
    /** 暴击时，下一次普攻额外目标选择 */
    private String critNormalExtraSelectId;
    /** 暴击时，下一次普攻额外目标伤害系数 */
    private double critNormalFactor;

}
