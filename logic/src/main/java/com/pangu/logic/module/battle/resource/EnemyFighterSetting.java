package com.pangu.logic.module.battle.resource;

import com.pangu.framework.resource.anno.Id;
import com.pangu.framework.resource.anno.Resource;
import lombok.Getter;

@Resource("battle")
@Getter
public class EnemyFighterSetting {

    @Id
    private String id;

    // 一组或者多组
    private String[] groups;

    //胜利场次
    private Integer winTimes;
}
