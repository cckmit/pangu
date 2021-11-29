package com.pangu.logic.module.battle.service.buff.param;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DamageOverTimeParam {

    /** 为true时只算一遍伤害，其它表示实时算伤害 */
    private boolean dmgFixed;
    /** 是否魔法伤害 */
    private boolean magic;
    /** 计算伤害方式 */
    private CalType calType;
    /** 参数内容 */
    private Object content;
    /** 目标选择器，允许dotBuff对周围辐射伤害 */
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
