package com.pangu.logic.module.battle.resource;

import com.pangu.framework.resource.anno.Id;
import com.pangu.framework.resource.anno.Resource;
import lombok.Getter;

@Resource("common")
@Getter
public class RoleSkin {

    // 模型ID
    @Id
    private int id;

    // 地图占格子大小
    private int scope;
}
