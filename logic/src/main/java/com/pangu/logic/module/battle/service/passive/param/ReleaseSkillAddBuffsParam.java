package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.model.SkillType;
import lombok.Getter;

@Getter
public class ReleaseSkillAddBuffsParam {
    //添加的BUFF
    private String[] buffs;

    //释放类型
    private Type type;

    public enum Type {
        //自己释放
        OWNER,

        //敌方释放
        ENEMY,

        //友军释放
        FRIEND
    }

}
