package com.pangu.logic.module.player.resource;
import com.pangu.framework.resource.anno.Id;
import com.pangu.framework.resource.anno.Resource;
import lombok.Getter;

@Resource("player")
@Getter
public class PlayerNameSetting {

    //玩家名
    @Id
    private String id;

}
