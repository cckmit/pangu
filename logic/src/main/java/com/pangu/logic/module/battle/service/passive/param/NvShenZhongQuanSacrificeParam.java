package com.pangu.logic.module.battle.service.passive.param;

import com.pangu.logic.module.battle.service.buff.param.DefaultAddValueParam;
import lombok.Getter;

@Getter
public class NvShenZhongQuanSacrificeParam {
    /** 每只小狗的献祭回复率*/
    private double recoverRate;

    /** 小狗死亡时修改召唤者属性*/
    private DefaultAddValueParam valModParam;
}
