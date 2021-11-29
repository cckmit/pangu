package com.pangu.logic.module.battle.service.passive.param;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MuShiFollowPassiveParam {

    private String buffId;

    private String zsBuffId;

    public MuShiFollowPassiveParam copy() {
        MuShiFollowPassiveParam param = new MuShiFollowPassiveParam();
        param.buffId = buffId;
        param.zsBuffId = zsBuffId;
        return param;
    }
}
