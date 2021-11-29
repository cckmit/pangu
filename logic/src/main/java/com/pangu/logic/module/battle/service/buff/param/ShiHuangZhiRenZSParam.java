package com.pangu.logic.module.battle.service.buff.param;

import com.pangu.logic.module.battle.service.passive.param.BuffUpdateParam;
import lombok.Getter;

@Getter
public class ShiHuangZhiRenZSParam {
    //[缓回]触发条件
    private int triggerCountOfHot;
    //[缓回]Buff
    private BuffUpdateParam hotBuff;
    //为友军添加的Buff
    private BuffUpdateParam buffForFriend;

    //扇形角度
    private int angleForFanShaped;
    //扇形半径
    private int radiusForFanShaped;
}
