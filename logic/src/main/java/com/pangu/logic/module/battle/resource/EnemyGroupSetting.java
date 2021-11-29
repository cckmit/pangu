package com.pangu.logic.module.battle.resource;

import com.pangu.framework.resource.anno.Id;
import com.pangu.framework.resource.anno.Resource;
import lombok.Getter;

/**
 * 组敌军配置信息
 */
@Resource("battle")
@Getter
public class EnemyGroupSetting {

    //  标识
    @Id
    private String id;

    //  敌军标识
    private String[] enemies;

    //  敌军覆盖名
    private String[] names;

    //随机加到一个怪物上的技能
    private String[] skills;
}
