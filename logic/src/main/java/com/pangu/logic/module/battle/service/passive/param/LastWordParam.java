package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.service.skill.param.DamageParam;
import lombok.Getter;

import java.util.Map;


@Getter
public class LastWordParam {
    private WordType wordType;
    //执行的技能
    private String skillId;
    //队友全部阵亡时是否执行复活
    private boolean revivableWhenAced = true;

    //<buffId,targetId>
    private Map<String, String> buffMap;

    //伤害类型参数
    private DamageParam dmg;
    private String dmgTarget;

    //遗言类型
    public enum WordType {
        //死亡时添加buff
        BUFF_CAST,
        //死亡时保留1点血并执行某个技能
        REVIVE,
        //死亡时提交一个世界效果
        WORLD_EFFECT

    }
}
