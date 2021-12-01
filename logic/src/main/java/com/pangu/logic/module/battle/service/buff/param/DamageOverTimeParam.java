package com.pangu.logic.module.battle.service.buff.param;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DamageOverTimeParam {


    private boolean dmgFixed;

    private boolean magic;

    private CalType calType;

    private Object content;

    private String targetId;

    public <T> T castContent() {
        return (T) content;
    }

    public enum CalType{
        //固定数值
        VALUE,
        //公式
        FORMULA,
        //rhino表达式
        EXP,
        //百分比攻击力的伤害
        SKILL,
        //由添加者实时传入
        DYNAMIC
    }

}
